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
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
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

    @PersistenceContext
    lateinit var entityManager: EntityManager

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
        entityManager.flush()
        entityManager.clear()

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
        Assert.assertEquals(0, persistedChildAgency.childAgencies.size)
    }

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithMemberStateAgency() {
        val tsStatusListEuResource = ClassPathResource("ts_status_list_eu.xml")
        val euAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        agencyService.updateTrustServiceListOperatorAgency(euAgency)
        entityManager.flush()
        entityManager.clear()

        val tsStatusListFrResource = ClassPathResource("ts_status_list_fr.xml")
        val topAgency = documentParsingService.parseTsStatusList(tsStatusListFrResource)
        agencyService.updateTrustServiceListOperatorAgency(topAgency)
        entityManager.flush()
        entityManager.clear()

        // Check the top agency has been updated
        val persistedTopAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode("FR") ?: throw IllegalStateException()
        val persistedEUAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode("EU") ?: throw IllegalStateException()
        Assert.assertEquals(persistedEUAgency, persistedTopAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_LIST_OPERATOR, persistedTopAgency.type)
        Assert.assertEquals(tsStatusListEuResource.url.toString(), persistedTopAgency.referencedByDocumentUrl)
        Assert.assertEquals(true, persistedTopAgency.isStillReferencedByDocument)
        Assert.assertEquals(2, persistedTopAgency.names.size)
        Assert.assertEquals(persistedTopAgency, persistedTopAgency.names[0].agency)
        Assert.assertEquals("fr", persistedTopAgency.names[0].languageCode)
        Assert.assertEquals("Agence nationale de la sécurité des systèmes d'information (ANSSI)", persistedTopAgency.names[0].name)
        Assert.assertEquals(1, persistedTopAgency.providingDocuments.size)
        Assert.assertEquals("http://www.ssi.gouv.fr/eidas/TL-FR.xml", persistedTopAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TS_STATUS_LIST_XML, persistedTopAgency.providingDocuments[0].type)
        Assert.assertEquals("en", persistedTopAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(persistedTopAgency, persistedTopAgency.providingDocuments[0].providedByAgency)
        Assert.assertEquals(true, persistedTopAgency.providingDocuments[0].isStillProvidedByAgency)
        Assert.assertEquals(22, persistedTopAgency.childAgencies.size)

        // Check the first child agency has been created
        val persistedChildAgency = persistedTopAgency.childAgencies[0]
        Assert.assertEquals(persistedTopAgency, persistedChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_PROVIDER, persistedChildAgency.type)
        Assert.assertEquals(tsStatusListFrResource.url.toString(), persistedChildAgency.referencedByDocumentUrl)
        Assert.assertEquals(true, persistedChildAgency.isStillReferencedByDocument)
        Assert.assertEquals(2, persistedChildAgency.names.size)
        Assert.assertEquals(persistedChildAgency, persistedChildAgency.names[0].agency)
        Assert.assertEquals("en", persistedChildAgency.names[0].languageCode)
        Assert.assertEquals("Agence Nationale des Titres Sécurisés", persistedChildAgency.names[0].name)
        Assert.assertEquals(0, persistedChildAgency.providingDocuments.size)
        Assert.assertEquals(8, persistedChildAgency.childAgencies.size)

        // Check the first grand child agency has been created
        val persistedGrandChildAgency = persistedChildAgency.childAgencies[0]
        Assert.assertEquals(persistedChildAgency, persistedGrandChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE, persistedGrandChildAgency.type)
        Assert.assertEquals(tsStatusListFrResource.url.toString(), persistedGrandChildAgency.referencedByDocumentUrl)
        Assert.assertEquals(true, persistedGrandChildAgency.isStillReferencedByDocument)
        Assert.assertEquals(2, persistedGrandChildAgency.names.size)
        Assert.assertEquals(persistedGrandChildAgency, persistedGrandChildAgency.names[0].agency)
        Assert.assertEquals("en", persistedGrandChildAgency.names[0].languageCode)
        Assert.assertEquals("Acteur de l'Administration d'Etat - Authentification 3 étoiles", persistedGrandChildAgency.names[0].name)
        Assert.assertEquals(2, persistedGrandChildAgency.providingDocuments.size)
        Assert.assertEquals("http://sp.ants.gouv.fr/antsv2/ANTS_AC_AAE_PC_v1.9.pdf", persistedGrandChildAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TSP_SERVICE_DEFINITION_PDF, persistedGrandChildAgency.providingDocuments[0].type)
        Assert.assertEquals("fr", persistedGrandChildAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(persistedGrandChildAgency, persistedGrandChildAgency.providingDocuments[0].providedByAgency)
        Assert.assertEquals(true, persistedGrandChildAgency.providingDocuments[0].isStillProvidedByAgency)
        Assert.assertEquals(0, persistedGrandChildAgency.childAgencies.size)
    }

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithMissingAndNewAgencyName() {
        // Populate the database with agencies
        val tsStatusListEuResource = ClassPathResource("ts_status_list_eu.xml")
        val euAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        agencyService.updateTrustServiceListOperatorAgency(euAgency)
        entityManager.flush()
        entityManager.clear()

        // Reload the same top agency, but add a new name and remove another one
        val modifiedEuAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        modifiedEuAgency.names = modifiedEuAgency.names.filter { it.languageCode != "fr" } // Remove the french name
        val englishAgencyName = modifiedEuAgency.names.findLast { it.languageCode == "en" }
        englishAgencyName?.name = "Modified name." // Modify the english name
        agencyService.updateTrustServiceListOperatorAgency(modifiedEuAgency)
        entityManager.flush()
        entityManager.clear()

        // Check the database was correctly updated
        val persistedEuAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode("EU") ?: throw IllegalStateException()
        Assert.assertEquals(22, persistedEuAgency.names.size)
        Assert.assertNull(persistedEuAgency.names.findLast { it.languageCode == "fr" })
        Assert.assertEquals("Modified name.", persistedEuAgency.names.findLast { it.languageCode == "en" }?.name)
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