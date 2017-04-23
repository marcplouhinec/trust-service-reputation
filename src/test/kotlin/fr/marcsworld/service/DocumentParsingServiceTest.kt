package fr.marcsworld.service

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.Agency
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.junit4.SpringRunner

/**
 * Test the [DocumentParsingService].
 *
 * @author Marc Plouhinec
 */
@RunWith(SpringRunner::class)
@SpringBootTest
class DocumentParsingServiceTest {

    @Autowired
    lateinit var documentParsingService: DocumentParsingService

    @Test
    fun testParseTsStatusListFromEuropeanCommission() {
        val tsStatusListEuResource = ClassPathResource("ts_status_list_eu.xml")
        val topAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)

        // Check the top agency
        Assert.assertNull(topAgency.id)
        Assert.assertNull(topAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_LIST_OPERATOR, topAgency.type)
        Assert.assertNull(topAgency.referencedByDocumentUrl)
        Assert.assertEquals("EU", topAgency.territoryCode)

        Assert.assertEquals(23, topAgency.names.size)
        Assert.assertNull(topAgency.names[0].id)
        Assert.assertEquals(topAgency, topAgency.names[0].agency)
        Assert.assertEquals("en", topAgency.names[0].languageCode)
        Assert.assertEquals("European Commission", topAgency.names[0].name)

        Assert.assertEquals(1, topAgency.providingDocuments.size)
        Assert.assertEquals("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml", topAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TS_STATUS_LIST_XML, topAgency.providingDocuments[0].type)
        Assert.assertEquals("en", topAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(topAgency, topAgency.providingDocuments[0].providedByAgency)

        // Check the first child agency
        Assert.assertEquals(31, topAgency.childAgencies.size)
        val firstChildAgency = topAgency.childAgencies[0]
        Assert.assertNull(firstChildAgency.id)
        Assert.assertEquals(topAgency, firstChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_LIST_OPERATOR, firstChildAgency.type)
        Assert.assertEquals(tsStatusListEuResource.url.toString(), firstChildAgency.referencedByDocumentUrl)
        Assert.assertEquals("AT", firstChildAgency.territoryCode)

        Assert.assertEquals(2, firstChildAgency.names.size)
        Assert.assertNull(firstChildAgency.names[0].id)
        Assert.assertEquals(firstChildAgency, firstChildAgency.names[0].agency)
        Assert.assertEquals("en", firstChildAgency.names[0].languageCode)
        Assert.assertEquals("Rundfunk und Telekom Regulierungs-GmbH", firstChildAgency.names[0].name)

        Assert.assertEquals(1, firstChildAgency.providingDocuments.size)
        Assert.assertNull(firstChildAgency.providingDocuments[0].id)
        Assert.assertEquals("https://www.signatur.rtr.at/currenttl.xml", firstChildAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TS_STATUS_LIST_XML, firstChildAgency.providingDocuments[0].type)
        Assert.assertEquals("en", firstChildAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(firstChildAgency, firstChildAgency.providingDocuments[0].providedByAgency)

        Assert.assertEquals(0, firstChildAgency.childAgencies.size)
    }

    @Test
    fun testParseTsStatusListFromMemberState() {
        val tsStatusListFrResource = ClassPathResource("ts_status_list_fr.xml")
        val topAgency = documentParsingService.parseTsStatusList(tsStatusListFrResource)

        // Check the top agency
        Assert.assertNull(topAgency.id)
        Assert.assertNull(topAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_LIST_OPERATOR, topAgency.type)
        Assert.assertNull(topAgency.referencedByDocumentUrl)
        Assert.assertEquals("FR", topAgency.territoryCode)

        Assert.assertEquals(2, topAgency.names.size)
        Assert.assertNull(topAgency.names[0].id)
        Assert.assertEquals(topAgency, topAgency.names[0].agency)
        Assert.assertEquals("en", topAgency.names[0].languageCode)
        Assert.assertEquals("French Network Information Security Agency", topAgency.names[0].name)

        Assert.assertEquals(0, topAgency.providingDocuments.size)

        // Check the first child agency
        Assert.assertEquals(22, topAgency.childAgencies.size)
        val firstChildAgency = topAgency.childAgencies[0]
        Assert.assertNull(firstChildAgency.id)
        Assert.assertEquals(topAgency, firstChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_PROVIDER, firstChildAgency.type)
        Assert.assertEquals(tsStatusListFrResource.url.toString(), firstChildAgency.referencedByDocumentUrl)
        Assert.assertNull(firstChildAgency.territoryCode)

        Assert.assertEquals(2, firstChildAgency.names.size)
        Assert.assertNull(firstChildAgency.names[0].id)
        Assert.assertEquals(firstChildAgency, firstChildAgency.names[0].agency)
        Assert.assertEquals("en", firstChildAgency.names[0].languageCode)
        Assert.assertEquals("Agence Nationale des Titres Sécurisés", firstChildAgency.names[0].name)

        Assert.assertEquals(0, firstChildAgency.providingDocuments.size)

        // Check the first grand child agency
        Assert.assertEquals(8, firstChildAgency.childAgencies.size)
        val firstGrandChildAgency = firstChildAgency.childAgencies[0]
        Assert.assertNull(firstGrandChildAgency.id)
        Assert.assertEquals(firstChildAgency, firstGrandChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE, firstGrandChildAgency.type)
        Assert.assertEquals(tsStatusListFrResource.url.toString(), firstGrandChildAgency.referencedByDocumentUrl)
        Assert.assertNull(firstGrandChildAgency.territoryCode)

        Assert.assertEquals(2, firstGrandChildAgency.names.size)
        Assert.assertNull(firstGrandChildAgency.names[0].id)
        Assert.assertEquals(firstGrandChildAgency, firstGrandChildAgency.names[0].agency)
        Assert.assertEquals("en", firstGrandChildAgency.names[0].languageCode)
        Assert.assertEquals("Acteur de l'Administration d'Etat - Authentification 3 étoiles", firstGrandChildAgency.names[0].name)

        Assert.assertEquals(2, firstGrandChildAgency.providingDocuments.size)
        Assert.assertNull(firstGrandChildAgency.providingDocuments[0].id)
        Assert.assertEquals("http://sp.ants.gouv.fr/antsv2/ANTS_AC_AAE_PC_v1.9.pdf", firstGrandChildAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TSP_SERVICE_DEFINITION, firstGrandChildAgency.providingDocuments[0].type)
        Assert.assertEquals("fr", firstGrandChildAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(firstGrandChildAgency, firstGrandChildAgency.providingDocuments[0].providedByAgency)

        Assert.assertEquals(0, firstGrandChildAgency.childAgencies.size)
    }

    @Test
    fun testParseTspServiceDefinition() {
        val tspServiceDefinitionResource = ClassPathResource("pc-certimetiersartisanat_v1.3_en.pdf")
        val testAgency = Agency(type = AgencyType.TRUST_SERVICE)
        val documents = documentParsingService.parseTspServiceDefinition(tspServiceDefinitionResource, testAgency)

        Assert.assertEquals(1, documents.size)
        Assert.assertNull(documents[0].id)
        Assert.assertEquals("http://lcr.certimetiersartisanat.fr/reference/certimetiersartisanat.crl", documents[0].url)
        Assert.assertEquals(DocumentType.CERTIFICATE_REVOCATION_LIST, documents[0].type)
        Assert.assertEquals("en", documents[0].languageCode)
        Assert.assertEquals(testAgency, documents[0].providedByAgency)

        // Try to parse a non-PDF file
        val nonPdfResource = ClassPathResource("ts_status_list_eu.xml")
        val documents2 = documentParsingService.parseTspServiceDefinition(nonPdfResource, testAgency)
        Assert.assertEquals(0, documents2.size)
    }
}