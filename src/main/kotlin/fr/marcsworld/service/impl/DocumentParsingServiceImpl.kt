package fr.marcsworld.service.impl

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.entity.Agency
import fr.marcsworld.model.entity.AgencyName
import fr.marcsworld.model.entity.Document
import fr.marcsworld.service.DocumentParsingService
import fr.marcsworld.utils.ResourceDownloader
import org.apache.pdfbox.io.RandomAccessBuffer
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXParseException
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import javax.xml.XMLConstants
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Default implementation of [DocumentParsingService].
 *
 * @author Marc Plouhinec
 */
@Service
class DocumentParsingServiceImpl : DocumentParsingService {

    companion object {
        val LOGGER = LoggerFactory.getLogger(DocumentParsingServiceImpl::class.java)!!
    }

    private val namespaceContext = TsStatusListNamespaceContext()

    override fun parseTsStatusList(resource: Resource): Agency {
        val resourceUrl = resource.url.toString()
        LOGGER.debug("Parse the trust service status list: {}", resourceUrl)

        // Parse the XML file in memory
        val documentByteArray = try {
            ResourceDownloader.downloadResource(resource)
        } catch (e: Exception) {
            LOGGER.warn("Unable to download the document: $resourceUrl", e)
            throw e
        }
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        documentBuilderFactory.isNamespaceAware = true
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document = try {
            documentByteArray.inputStream().buffered().use {
                documentBuilder.parse(it)
            }
        } catch (e: SAXParseException) {
            LOGGER.warn("Invalid XML document from: $resourceUrl", e)
            throw e
        }

        // Parse the TRUST_SERVICE_LIST_OPERATOR agency
        val topTerritoryCode = evalXPathToString(document, "/v2:TrustServiceStatusList/v2:SchemeInformation/v2:SchemeTerritory/text()")
        val topAgency = Agency(
                type = AgencyType.TRUST_SERVICE_LIST_OPERATOR,
                territoryCode = topTerritoryCode ?: throw IllegalArgumentException("Missing SchemeTerritory."))
        topAgency.names = evalXPathToAgencyNames(document, "/v2:TrustServiceStatusList/v2:SchemeInformation/v2:SchemeOperatorName/v2:Name", topAgency)

        // Parse the children TRUST_SERVICE_LIST_OPERATOR agencies
        val tslPointerNodes = evalXPathToNodes(document, "/v2:TrustServiceStatusList/v2:SchemeInformation/v2:PointersToOtherTSL/v2:OtherTSLPointer" +
                "[v2:AdditionalInformation/v2:OtherInformation/additionaltypes:MimeType=\"application/vnd.etsi.tsl+xml\"]")
        val otherTsloAgencies = tslPointerNodes
                .map {
                    val childTerritoryCode = evalXPathToString(it, "./v2:AdditionalInformation/v2:OtherInformation/v2:SchemeTerritory/text()")
                    val agency = Agency(
                            parentAgency = topAgency,
                            type = AgencyType.TRUST_SERVICE_LIST_OPERATOR,
                            territoryCode = childTerritoryCode ?: throw IllegalArgumentException("Missing SchemeTerritory."),
                            referencedByDocumentUrl = resourceUrl)
                    agency.names = evalXPathToAgencyNames(it, "./v2:AdditionalInformation/v2:OtherInformation/v2:SchemeOperatorName/v2:Name", agency)
                    val tslLocation = evalXPathToString(it, "./v2:TSLLocation/text()")
                    if (tslLocation is String) {
                        agency.providingDocuments = listOf(Document(
                                url = tslLocation,
                                type = DocumentType.TS_STATUS_LIST_XML,
                                languageCode = "en",
                                providedByAgency = agency))
                    }

                    agency
                }
        val memberStateTsloAgencies = otherTsloAgencies.filter { it.territoryCode != "EU" }
        if (topTerritoryCode == "EU") {
            val euTsloAgency = otherTsloAgencies.findLast { it.territoryCode == "EU" }
            if (euTsloAgency is Agency) {
                topAgency.providingDocuments = euTsloAgency.providingDocuments.map { Document(url = it.url, type = it.type, languageCode = it.languageCode, providedByAgency = topAgency) }
            }
        }

        // Parse the TRUST_SERVICE_PROVIDER agencies
        val tspNodes = evalXPathToNodes(document, "/v2:TrustServiceStatusList/v2:TrustServiceProviderList/v2:TrustServiceProvider")
        val childrenTspAgencies = tspNodes.map {
            val tspAgency = Agency(
                    parentAgency = topAgency,
                    type = AgencyType.TRUST_SERVICE_PROVIDER,
                    referencedByDocumentUrl = resourceUrl)
            tspAgency.names = evalXPathToAgencyNames(it, "./v2:TSPInformation/v2:TSPName/v2:Name", tspAgency)

            // Parse the children TRUST_SERVICE agencies
            val tspServiceNode = evalXPathToNodes(it, "./v2:TSPServices/v2:TSPService")
            tspAgency.childrenAgencies = tspServiceNode
                    .map {
                        val tsAgency = Agency(
                                parentAgency = tspAgency,
                                type = AgencyType.TRUST_SERVICE,
                                referencedByDocumentUrl = resourceUrl
                        )
                        tsAgency.names = evalXPathToAgencyNames(it, "./v2:ServiceInformation/v2:ServiceName/v2:Name", tsAgency)

                        val uriNodes = evalXPathToNodes(it, "./v2:ServiceInformation/v2:TSPServiceDefinitionURI/v2:URI")
                        val uriNodes2 = evalXPathToNodes(it, "./v2:ServiceInformation/v2:SchemeServiceDefinitionURI/v2:URI")
                        val allUriNodes = uriNodes + uriNodes2
                        tsAgency.providingDocuments = allUriNodes.map {
                            Document(
                                    url = it.textContent,
                                    type = DocumentType.TSP_SERVICE_DEFINITION,
                                    languageCode = it.attributes.getNamedItem("xml:lang").nodeValue ?: throw IllegalArgumentException("Missing @xml:lang."),
                                    providedByAgency = tsAgency)
                        }

                        tsAgency
                    }
                    .filter { tsAgency ->
                        tsAgency.names.isNotEmpty()
                    }

            tspAgency
        }

        topAgency.childrenAgencies = memberStateTsloAgencies + childrenTspAgencies

        return topAgency
    }

    override fun parseTspServiceDefinition(resource: Resource, providerAgency: Agency): List<Document> {
        LOGGER.debug("Parse the TSP service definition: {}", resource.url)

        val documentByteArray = try {
            ResourceDownloader.downloadResource(resource)
        } catch (e: Exception) {
            LOGGER.warn("Unable to download the document: ${resource.url}", e)
            throw e
        }

        val singleLinePdfText = documentByteArray.inputStream().use {
            try {
                val pdfParser = PDFParser(RandomAccessBuffer(it))
                pdfParser.parse()

                var singleLinePdfText: String = ""
                var pdDocument: PDDocument? = null
                try {
                    pdDocument = PDDocument(pdfParser.document)
                    val pdfText: String? = PDFTextStripper().getText(pdDocument)
                    if (pdfText is String) {
                        singleLinePdfText = pdfText.replace("\n", "").toLowerCase()
                    }
                } finally {
                    pdfParser.document.close()
                    pdDocument?.close()
                }

                singleLinePdfText
            } catch (e: IOException) {
                // This is not a PDF file, so just try to open it like a text file
                documentByteArray.toString(Charset.forName("UTF-8")).replace("\n", "").toLowerCase()
            }
        }

        val crlRegex = """http[A-Za-z0-9\-._~:/?#\[\]@!$&'()*+,;=%]{1,2000}\.crl""".toRegex()
        val matchResults = crlRegex.findAll(singleLinePdfText)
        val documentUrls = matchResults
                .map { it.value }
                .distinct()

        return documentUrls
                .filter {
                    // Make sure the URL is valid
                    try {
                        URL(it).toURI()
                        true
                    } catch (e: Exception) {
                        LOGGER.error("The URL '$it' extracted from ${resource.url} is not valid.", e)
                        false
                    }
                }
                .map {
                    Document(
                            url = it,
                            type = DocumentType.CERTIFICATE_REVOCATION_LIST,
                            languageCode = "en",
                            providedByAgency = providerAgency
                    )
                }
                .toList()
    }

    private fun evalXPathToString(node: Node, expression: String): String? {
        val xPath = XPathFactory.newInstance().newXPath()
        xPath.namespaceContext = namespaceContext
        val xPathExpression = xPath.compile(expression)
        val value = xPathExpression.evaluate(node, XPathConstants.STRING)
        return when (value) {
            is String -> value
            else -> null
        }
    }

    private fun evalXPathToNodes(node: Node, expression: String): List<Node> {
        val xPath = XPathFactory.newInstance().newXPath()
        xPath.namespaceContext = namespaceContext
        val xPathExpression = xPath.compile(expression)
        val nodeSet = xPathExpression.evaluate(node, XPathConstants.NODESET)
        return when (nodeSet) {
            is NodeList -> (0..nodeSet.length - 1).map { nodeSet.item(it) }
            else -> listOf()
        }
    }

    private fun evalXPathToAgencyNames(node: Node, expression: String, agency: Agency): List<AgencyName> {
        val nameNodes = evalXPathToNodes(node, expression)
        return nameNodes.map {
            AgencyName(
                    agency = agency,
                    languageCode = it.attributes.getNamedItem("xml:lang").nodeValue ?: throw IllegalArgumentException("Missing @xml:lang."),
                    name = it.textContent ?: throw IllegalArgumentException("Missing Name text."))
        }
    }

    /**
     * [NamespaceContext] compatible with documents of the type [DocumentType.TS_STATUS_LIST_XML].
     */
    private class TsStatusListNamespaceContext : NamespaceContext {
        override fun getNamespaceURI(prefix: String?): String {
            return when (prefix) {
                !is String -> throw NullPointerException("Null prefix")
                "v2" -> "http://uri.etsi.org/02231/v2#"
                "additionaltypes" -> "http://uri.etsi.org/02231/v2/additionaltypes#"
                "xml" -> XMLConstants.XML_NS_URI
                else -> XMLConstants.NULL_NS_URI
            }
        }

        override fun getPrefix(namespaceURI: String?): String {
            throw UnsupportedOperationException()
        }

        override fun getPrefixes(namespaceURI: String?): MutableIterator<Any?> {
            throw UnsupportedOperationException()
        }
    }
}