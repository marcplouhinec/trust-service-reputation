package fr.marcsworld.service

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.entity.Agency
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
        Assert.assertEquals(31, topAgency.childrenAgencies.size)
        val firstChildAgency = topAgency.childrenAgencies[0]
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

        Assert.assertEquals(0, firstChildAgency.childrenAgencies.size)
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
        Assert.assertEquals(22, topAgency.childrenAgencies.size)
        val firstChildAgency = topAgency.childrenAgencies[0]
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
        Assert.assertEquals(8, firstChildAgency.childrenAgencies.size)
        val firstGrandChildAgency = firstChildAgency.childrenAgencies[0]
        Assert.assertNull(firstGrandChildAgency.id)
        Assert.assertEquals(firstChildAgency, firstGrandChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE, firstGrandChildAgency.type)
        Assert.assertEquals(tsStatusListFrResource.url.toString(), firstGrandChildAgency.referencedByDocumentUrl)
        Assert.assertNull(firstGrandChildAgency.territoryCode)
        Assert.assertEquals(
                "MIIFPTCCAyWgAwIBAgISESDUY1mis16tVaM/IV/i71HSMA0GCSqGSIb3DQEBCwUAMGIxCzAJBgNVBAYTAkZSMQ0" +
                        "wCwYDVQQKEwRHb3V2MRcwFQYDVQQLEw4wMDAyIDEzMDAwMzI2MjErMCkGA1UEAwwiQXV0b3JpdMOpIG" +
                        "RlIGNlcnRpZmljYXRpb24gQU5UUyBWMjAeFw0xMTExMTcwMDAwMDBaFw0xNzExMTcwMDAwMDBaMIGUM" +
                        "QswCQYDVQQGEwJGUjEwMC4GA1UECgwnQWdlbmNlIE5hdGlvbmFsZSBkZXMgVGl0cmVzIFPDqWN1cmlz" +
                        "w6lzMRcwFQYDVQQLEw4wMDAyIDEzMDAwMzI2MjE6MDgGA1UEAwwxQXV0b3JpdMOpIGRlIGNlcnRpZml" +
                        "jYXRpb24gcG9ydGV1ciBBQUUgMyDDqXRvaWxlczCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCgg" +
                        "EBALAWpHdvI/r1XIyfyyYQJv59Qv/EN0Dg1mJ8VqeUsG9Ewx9K+PN5ZiMH032tIyTRKUGWNJmbuFl0c" +
                        "8X9CmNq0TGFpF3uOgiAGNKHALAAH1lw5JxD3VTXTTbJdnjtvIRstL9xOo2vtqaJo5fq2KlqM+7kFS+I" +
                        "HCrodjuLWSMBbz8gJdxKHoJA7uvWloqjqPp+yD/vrN6W4nm56SN3lIDB3LGVNmGPilvbjB33nGbeQoh" +
                        "gJFwiLYvHl/mmIUl8OClVc99d9weL+Q6zXtW/Jb+8Bp4vR7ycpF1g6Q2FasScMvinstYGwR94qoKwPD" +
                        "O+UoswQnRAlC37lUIEeaFsErJle08CAwEAAaOBuTCBtjAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/B" +
                        "AgwBgEB/wIBADA9BgNVHR8ENjA0MDKgMKAuhixodHRwOi8vY3JsLmFudHMuZ291di5mci9hbnRzdjIv" +
                        "YWNfcmFjaW5lLmNybDARBgNVHSAECjAIMAYGBFUdIAAwHQYDVR0OBBYEFK0tbwAZg6k4G3+HI+/DwPd" +
                        "9qCVJMB8GA1UdIwQYMBaAFF0cxN5nSe9GUxwcVP+1yQdbClkJMA0GCSqGSIb3DQEBCwUAA4ICAQA0de" +
                        "lGzyhLv8n7jJz7wpG3vGfskEOCDvu1y0BwAFwxpoW6a0lOupXrXRC1NUybqwlJBv445buRnmd+xIhKo" +
                        "PhdirHMGUR3CBD15AwZyspPKLgfsG9Pv6aBfBc3bLRdP/jW7TOoQZ2MsGRhHqv62ZjUTxJeFGMo+egM" +
                        "eGUQNz9HtHXZjuBH0bevCr+QqlyPK/rhG+WhU2p2C/fd7AgrBA1CyKn3cHRB3nMKO8hx3Hh+wNdhsku" +
                        "N79cAgOQvGabexiIEDFnf99IZ4JhnO7hkVuz+Ahs5BqxMhCHeVUGTSzEIwda6nTdgHB+6sn0d6Lcser" +
                        "l3J/PlMhJzg2h1/qNgbqckz9u+xERaspBo15PwPhe1mGpz8iY6ckveKBVeAEPjtEgovT0819tARjAT+" +
                        "IiNrmUQF0UOGL152yCDRWPBeprbYgEf7B/6JZS6l0tDMkq6fhiQR18zsz6WnAMk4IjsPddtmEyIaPqo" +
                        "D39kUO57OKw4uJbOsGopl35bZ1SnLAk/TRFsV2kEsysA4CXh+YSSEGQpKigDqJzQFqwwT6xBxxuxmz6" +
                        "wZA5i1ki1UECgDzLrkD4unGF6ri8lzu8wYrLVMb/6IoC/PXaV8wuDbXzYXYgCGPDWrNlxfrPSI/a5MW" +
                        "5cOq7+dwH9NIo3ome1Rk7G2/dv2ANpJmm0O7y31rnCgKI1gQ==",
                firstGrandChildAgency.x509Certificate)

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

        Assert.assertEquals(0, firstGrandChildAgency.childrenAgencies.size)
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