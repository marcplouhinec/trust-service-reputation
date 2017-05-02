package fr.marcsworld.service

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.entity.Agency
import fr.marcsworld.model.entity.AgencyName
import fr.marcsworld.model.entity.Document
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
        Assert.assertEquals(31, persistedTopAgency.childrenAgencies.size)

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
        Assert.assertEquals(0, persistedChildAgency.childrenAgencies.size)
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
        Assert.assertEquals(22, persistedTopAgency.childrenAgencies.size)

        // Check the first child agency has been created
        val persistedChildAgency = persistedTopAgency.childrenAgencies[0]
        Assert.assertEquals(persistedTopAgency, persistedChildAgency.parentAgency)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_PROVIDER, persistedChildAgency.type)
        Assert.assertEquals(tsStatusListFrResource.url.toString(), persistedChildAgency.referencedByDocumentUrl)
        Assert.assertEquals(true, persistedChildAgency.isStillReferencedByDocument)
        Assert.assertEquals(2, persistedChildAgency.names.size)
        Assert.assertEquals(persistedChildAgency, persistedChildAgency.names[0].agency)
        Assert.assertEquals("en", persistedChildAgency.names[0].languageCode)
        Assert.assertEquals("Agence Nationale des Titres Sécurisés", persistedChildAgency.names[0].name)
        Assert.assertEquals(0, persistedChildAgency.providingDocuments.size)
        Assert.assertEquals(8, persistedChildAgency.childrenAgencies.size)

        // Check the first grand child agency has been created
        val persistedGrandChildAgency = persistedChildAgency.childrenAgencies[0]
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
        Assert.assertEquals(DocumentType.TSP_SERVICE_DEFINITION, persistedGrandChildAgency.providingDocuments[0].type)
        Assert.assertEquals("fr", persistedGrandChildAgency.providingDocuments[0].languageCode)
        Assert.assertEquals(persistedGrandChildAgency, persistedGrandChildAgency.providingDocuments[0].providedByAgency)
        Assert.assertEquals(true, persistedGrandChildAgency.providingDocuments[0].isStillProvidedByAgency)
        Assert.assertEquals(0, persistedGrandChildAgency.childrenAgencies.size)
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
        // Populate the database with agencies
        val tsStatusListEuResource = ClassPathResource("ts_status_list_eu.xml")
        val euAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        agencyService.updateTrustServiceListOperatorAgency(euAgency)
        entityManager.flush()
        entityManager.clear()

        // Reload the same agencies, but modify their documents
        val modifiedEuAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        modifiedEuAgency.childrenAgencies[0].providingDocuments += Document( // Add a new document to the first child agency
                url = "http://example.org",
                type = DocumentType.TS_STATUS_LIST_XML,
                languageCode = "en",
                providedByAgency = modifiedEuAgency.childrenAgencies[0]
        )
        modifiedEuAgency.childrenAgencies[1].providingDocuments = listOf() // Remove the documents of the second child agency
        modifiedEuAgency.childrenAgencies[2].providingDocuments[0].languageCode = "zz" // Modify the document of the third child agency
        agencyService.updateTrustServiceListOperatorAgency(modifiedEuAgency)
        entityManager.flush()
        entityManager.clear()

        // Check the database was correctly updated
        val persistedEuAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode("EU") ?: throw IllegalStateException()
        Assert.assertEquals(2, persistedEuAgency.childrenAgencies[0].providingDocuments.size)
        Assert.assertEquals("http://example.org", persistedEuAgency.childrenAgencies[0].providingDocuments[1].url)
        Assert.assertEquals(1, persistedEuAgency.childrenAgencies[1].providingDocuments.size)
        Assert.assertEquals(false, persistedEuAgency.childrenAgencies[1].providingDocuments[0].isStillProvidedByAgency)
        Assert.assertEquals(1, persistedEuAgency.childrenAgencies[2].providingDocuments.size)
        Assert.assertEquals("zz", persistedEuAgency.childrenAgencies[2].providingDocuments[0].languageCode)

        // Reload the same agencies again, but this time restore the original documents
        val euAgency2 = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        agencyService.updateTrustServiceListOperatorAgency(euAgency2)
        entityManager.flush()
        entityManager.clear()

        // Check the database was correctly updated
        val persistedEuAgency2 = agencyRepository.findTrustServiceListOperatorByTerritoryCode("EU") ?: throw IllegalStateException()
        Assert.assertEquals(1, persistedEuAgency2.childrenAgencies[1].providingDocuments.size)
        Assert.assertEquals(true, persistedEuAgency2.childrenAgencies[1].providingDocuments[0].isStillProvidedByAgency)
    }

    @Test
    fun testUpdateTrustServiceListOperatorAgencyWithNewModifiedMissingAndFoundAgainChildAgency() {
        // Populate the database with agencies
        val tsStatusListEuResource = ClassPathResource("ts_status_list_eu.xml")
        val euAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        agencyService.updateTrustServiceListOperatorAgency(euAgency)
        entityManager.flush()
        entityManager.clear()

        // Reload the same agencies, but modify the children: add a new agency, delete one and modify another
        val modifiedEuAgency = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        val newAgency = Agency(
                parentAgency = modifiedEuAgency,
                type = AgencyType.TRUST_SERVICE_LIST_OPERATOR,
                referencedByDocumentUrl = "http://www.example.org",
                territoryCode = "ZZ"
        )
        newAgency.names = listOf(AgencyName(agency = newAgency, name = "Example", languageCode = "en"))
        newAgency.providingDocuments = listOf(Document(url = "http://www.example.com", type = DocumentType.TS_STATUS_LIST_XML, languageCode = "en", providedByAgency = newAgency))
        modifiedEuAgency.childrenAgencies += newAgency

        modifiedEuAgency.childrenAgencies = modifiedEuAgency.childrenAgencies.filter { it.territoryCode != "FR" }

        val atAgency = modifiedEuAgency.childrenAgencies.findLast { it.territoryCode == "AT" }
        atAgency?.referencedByDocumentUrl = "http://www.example.net"

        agencyService.updateTrustServiceListOperatorAgency(modifiedEuAgency)
        entityManager.flush()
        entityManager.clear()

        // Check the database was correctly updated
        val persistedEuAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode("EU") ?: throw IllegalStateException()
        Assert.assertEquals(32, persistedEuAgency.childrenAgencies.size)

        val lastChildAgency = persistedEuAgency.childrenAgencies[persistedEuAgency.childrenAgencies.size - 1]
        Assert.assertEquals("ZZ", lastChildAgency.territoryCode)
        Assert.assertEquals("http://www.example.org", lastChildAgency.referencedByDocumentUrl)
        Assert.assertEquals(AgencyType.TRUST_SERVICE_LIST_OPERATOR, lastChildAgency.type)
        Assert.assertEquals(persistedEuAgency, lastChildAgency.parentAgency)
        Assert.assertEquals(1, lastChildAgency.names.size)
        Assert.assertEquals("en", lastChildAgency.names[0].languageCode)
        Assert.assertEquals("Example", lastChildAgency.names[0].name)
        Assert.assertEquals(1, lastChildAgency.providingDocuments.size)
        Assert.assertEquals("en", lastChildAgency.providingDocuments[0].languageCode)
        Assert.assertEquals("http://www.example.com", lastChildAgency.providingDocuments[0].url)
        Assert.assertEquals(DocumentType.TS_STATUS_LIST_XML, lastChildAgency.providingDocuments[0].type)

        val frChildAgency = persistedEuAgency.childrenAgencies.findLast { it.territoryCode == "FR" }
        Assert.assertEquals(false, frChildAgency?.isStillReferencedByDocument)

        val atChildAgency = persistedEuAgency.childrenAgencies.findLast { it.territoryCode == "AT" }
        Assert.assertEquals("http://www.example.net", atChildAgency?.referencedByDocumentUrl)

        // Reload the same agencies again, but this time restore the original documents
        val euAgency2 = documentParsingService.parseTsStatusList(tsStatusListEuResource)
        agencyService.updateTrustServiceListOperatorAgency(euAgency2)
        entityManager.flush()
        entityManager.clear()

        // Check the database was correctly updated
        val persistedEuAgency2 = agencyRepository.findTrustServiceListOperatorByTerritoryCode("EU") ?: throw IllegalStateException()
        Assert.assertEquals(32, persistedEuAgency2.childrenAgencies.size)

        val frChildAgency2 = persistedEuAgency2.childrenAgencies.findLast { it.territoryCode == "FR" }
        Assert.assertEquals(true, frChildAgency2?.isStillReferencedByDocument)
    }
}