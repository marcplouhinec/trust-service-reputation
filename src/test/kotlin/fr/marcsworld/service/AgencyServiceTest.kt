package fr.marcsworld.service

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
import fr.marcsworld.repository.AgencyRepository
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.junit4.SpringRunner
import javax.transaction.Transactional

/**
 * Test for the [AgencyService].
 *
 * @author Marc Plouhinec
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
open class AgencyServiceTest {

    @Autowired
    lateinit var agencyService : AgencyService

    @Autowired
    lateinit var documentParsingService: DocumentParsingService

    @Autowired
    lateinit var agencyRepository: AgencyRepository

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithEuropeanCommissionAgency() {
        val tsStatusListEuResource = ClassPathResource("ts_status_list_eu.xml")
        val topAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)

        agencyService.updateTrustServiceListOperatorAgency(topAgency)

        // Check the top agency has been updated
        val persistedTopAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode("EU") ?: throw IllegalStateException()
        Assert.assertNull(persistedTopAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_LIST_OPERATOR, persistedTopAgency.type)
        Assert.assertNull(persistedTopAgency.referencedByDocumentUrl)
        Assert.assertNull(persistedTopAgency.isStillReferencedByDocument)
        Assert.assertEquals(23, persistedTopAgency.names.size)
        Assert.assertEquals(persistedTopAgency, persistedTopAgency.names[0].agency)
        Assert.assertEquals("en", persistedTopAgency.names[0].languageCode)
        Assert.assertEquals("European Commission", persistedTopAgency.names[0].name)
        Assert.assertEquals(1, persistedTopAgency.providingDocuments.size)
        Assert.assertEquals("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml", persistedTopAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TS_STATUS_LIST_XML, persistedTopAgency.providingDocuments[0].type)
        Assert.assertEquals("en", persistedTopAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(persistedTopAgency, persistedTopAgency.providingDocuments[0].providedByAgency)
        Assert.assertEquals(true, persistedTopAgency.providingDocuments[0].isStillProvidedByAgency)
        Assert.assertNotNull(persistedTopAgency.providingDocuments[0].version)
        Assert.assertEquals(31, persistedTopAgency.childAgencies.size)

        // Check the first child agency has been created
        val persistedChildAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode("AT") ?: throw IllegalStateException()
        Assert.assertEquals(persistedTopAgency, persistedChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_LIST_OPERATOR, persistedChildAgency.type)
        Assert.assertEquals(tsStatusListEuResource.url.toString(), persistedChildAgency.referencedByDocumentUrl)
        Assert.assertEquals(true, persistedChildAgency.isStillReferencedByDocument)
        Assert.assertEquals(2, persistedChildAgency.names.size)
        Assert.assertEquals(persistedChildAgency, persistedChildAgency.names[0].agency)
        Assert.assertEquals("en", persistedChildAgency.names[0].languageCode)
        Assert.assertEquals("Rundfunk und Telekom Regulierungs-GmbH", persistedChildAgency.names[0].name)
        Assert.assertEquals(1, persistedChildAgency.providingDocuments.size)
        Assert.assertEquals("https://www.signatur.rtr.at/currenttl.xml", persistedChildAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TS_STATUS_LIST_XML, persistedChildAgency.providingDocuments[0].type)
        Assert.assertEquals("en", persistedChildAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(persistedChildAgency, persistedChildAgency.providingDocuments[0].providedByAgency)
        Assert.assertEquals(true, persistedChildAgency.providingDocuments[0].isStillProvidedByAgency)
        Assert.assertNotNull(persistedChildAgency.providingDocuments[0].version)
        Assert.assertEquals(0, persistedChildAgency.childAgencies.size)
    }

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithMemberStateAgency() {
        TODO()
    }

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithMissingAndNewAgencyName() {
        TODO()
    }

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithNewModifiedMissingAndFoundAgainDocument() {
        TODO()
    }

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithNewModifiedMissingAndFoundAgainChildAgency() {
        TODO()
    }
}