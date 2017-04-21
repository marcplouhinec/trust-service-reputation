package fr.marcsworld.service

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
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
        Assert.assertEquals(topAgency, topAgency.providingDocuments[0].providerAgency)

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
        Assert.assertEquals("https://www.signatur.rtr.at/currenttl.xml", firstChildAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TS_STATUS_LIST_XML, firstChildAgency.providingDocuments[0].type)
        Assert.assertEquals("en", firstChildAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(firstChildAgency, firstChildAgency.providingDocuments[0].providerAgency)

        Assert.assertEquals(0, firstChildAgency.childAgencies.size)
    }

    @Test
    fun testParseTsStatusListFromMemberState() {
        // TODO
    }

}