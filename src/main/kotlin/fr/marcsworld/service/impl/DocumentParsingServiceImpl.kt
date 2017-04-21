package fr.marcsworld.service.impl

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.Agency
import fr.marcsworld.model.AgencyName
import fr.marcsworld.model.Document
import fr.marcsworld.service.DocumentParsingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.net.URL
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

    override fun parseTsStatusList(url: String): Agency {
        LOGGER.info("Parse the trust service status list: {}", url)

        // Parse the XML file in memory
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        documentBuilderFactory.isNamespaceAware = true
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document = URL(url).openStream().buffered().use {
            documentBuilder.parse(it)
        }

        // Parse the TRUST_SERVICE_LIST_OPERATOR agency
        val territoryCode = evalXPathToString(
                document, "/v2:TrustServiceStatusList/v2:SchemeInformation/v2:SchemeTerritory/text()")
        val topAgency = Agency(
                type = AgencyType.TRUST_SERVICE_LIST_OPERATOR,
                territoryCode = territoryCode ?: "UNKNOWN")
        topAgency.names = evalXPathToAgencyNames(
                document, "/v2:TrustServiceStatusList/v2:SchemeInformation/v2:SchemeOperatorName/v2:Name", topAgency)

        // Parse the children TRUST_SERVICE_LIST_OPERATOR agencies
        val tslPointerNodes = evalXPathToNodes(
                document, "/v2:TrustServiceStatusList/v2:SchemeInformation/v2:PointersToOtherTSL/v2:OtherTSLPointer" +
                "[v2:AdditionalInformation/v2:OtherInformation/additionaltypes:MimeType=\"application/vnd.etsi.tsl+xml\"]")
        topAgency.childAgencies = tslPointerNodes
                .map {
                    val childTerritoryCode = evalXPathToString(
                            it, "./v2:AdditionalInformation/v2:OtherInformation/v2:SchemeTerritory/text()")
                    val agency = Agency(
                            parentAgency = topAgency,
                            type = AgencyType.TRUST_SERVICE_LIST_OPERATOR,
                            territoryCode = childTerritoryCode ?: "UNKNOWN",
                            referencingDocumentUrl = url)
                    agency.names = evalXPathToAgencyNames(
                            it, "./v2:AdditionalInformation/v2:OtherInformation/v2:SchemeOperatorName/v2:Name", agency)
                    val tslLocation = evalXPathToString(it, "./v2:TSLLocation/text()")
                    if (tslLocation is String) {
                        agency.providingDocuments = listOf(Document(
                                url = tslLocation,
                                type = DocumentType.TS_STATUS_LIST_XML,
                                languageCode = "en",
                                providerAgency = agency))
                    }

                    agency
                }
                .filter { it.providingDocuments.any { it.url == url } }

        // Parse the TRUST_SERVICE_PROVIDER agencies
        val tspNodes = evalXPathToNodes(
                document, "/v2:TrustServiceStatusList/v2:TrustServiceProviderList/v2:TrustServiceProvider")
        topAgency.childAgencies = tspNodes.map {
            val tspAgency = Agency(
                    parentAgency = topAgency,
                    type = AgencyType.TRUST_SERVICE_PROVIDER,
                    referencingDocumentUrl = url)
            tspAgency.names = evalXPathToAgencyNames(it, "./v2:TSPInformation/v2:TSPTradeName/v2:Name", tspAgency)

            // Parse the children TRUST_SERVICE agencies
            val tspServiceNode = evalXPathToNodes(it, "./v2:TSPServices/v2:TSPService")
            tspAgency.childAgencies = tspServiceNode.map {
                val tsAgency = Agency(
                        parentAgency = tspAgency,
                        type = AgencyType.TRUST_SERVICE,
                        referencingDocumentUrl = url
                )
                tsAgency.names = evalXPathToAgencyNames(it, "./v2:ServiceInformation/v2:ServiceName/v2:Name", tsAgency)

                val uriNodes = evalXPathToNodes(it, "./v2:ServiceInformation/v2:TSPServiceDefinitionURI/v2:URI")
                tsAgency.providingDocuments = uriNodes.map {
                    Document(
                            url = it.textContent,
                            type = DocumentType.TSP_SERVICE_DEFINITION_PDF,
                            languageCode = evalXPathToString(it, "./@xml:lang") ?: "UNKNOWN",
                            providerAgency = tsAgency)
                }

                tsAgency
            }

            tspAgency
        }

        return topAgency
    }

    override fun parseTspServiceDefinition(url: String): List<Document> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            is NodeList -> (0..nodeSet.length).map { nodeSet.item(it) }
            else -> listOf()
        }
    }

    private fun evalXPathToAgencyNames(node: Node, expression: String, agency: Agency): List<AgencyName> {
        val nameNodes = evalXPathToNodes(node, expression)
        return nameNodes.map {
            AgencyName(
                    agency = agency,
                    languageCode = evalXPathToString(it, "./@xml:lang") ?: "UNKNOWN",
                    name = it.textContent ?: "UNKNOWN")
        }
    }

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