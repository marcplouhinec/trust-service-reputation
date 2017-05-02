package fr.marcsworld.model.entity

import fr.marcsworld.enums.AgencyType
import javax.persistence.*

/**
 * Agency that deals with Trust Services.
 *
 * @author Marc Plouhinec
 */
@Entity
@Table(name = "AGENCY")
class Agency (
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "AGENCY_ID", nullable = false)
        var id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "PARENT_AGENCY_ID", nullable = true)
        var parentAgency: Agency? = null,

        @Enumerated(EnumType.STRING)
        @Column(name = "AGENCY_TYPE", nullable = false, length = 32)
        var type: AgencyType,

        @Column(name = "REFERENCED_BY_DOCUMENT_URL", nullable = true)
        var referencedByDocumentUrl: String? = null,

        @Column(name = "IS_STILL_REFERENCED_BY_DOCUMENT", nullable = true)
        var isStillReferencedByDocument: Boolean? = true,

        @Column(name = "TERRITORY_CODE", nullable = true, length = 20)
        var territoryCode: String? = null,

        @OneToMany(mappedBy = "agency", fetch = FetchType.EAGER)
        var names: List<AgencyName> = listOf(),

        @OneToMany(mappedBy = "providedByAgency", fetch = FetchType.LAZY)
        var providingDocuments: List<Document> = listOf(),

        @OneToMany(mappedBy = "parentAgency", fetch = FetchType.LAZY)
        var childrenAgencies: List<Agency> = listOf()
)