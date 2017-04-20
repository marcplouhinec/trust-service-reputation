package fr.marcsworld.model

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
        var id: Long,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "PARENT_AGENCY_ID", nullable = true)
        var parentAgency: Agency,

        @Enumerated(EnumType.STRING)
        @Column(name = "AGENCY_TYPE", nullable = false, length = 32)
        var type: AgencyType,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "REFERENCING_DOCUMENT_URL", nullable = true)
        var referencingDocument: Document,

        @Column(name = "TERRITORY_CODE", nullable = true, length = 20)
        var territoryCode: String,

        @OneToMany(mappedBy = "agency", fetch = FetchType.EAGER)
        var names: List<AgencyName>,

        @OneToMany(mappedBy = "providerAgency", fetch = FetchType.LAZY)
        var providingDocuments: List<Document>
)