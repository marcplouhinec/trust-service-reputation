package fr.marcsworld.model.entity

import fr.marcsworld.enums.DocumentType
import javax.persistence.*

/**
 * Entity containing information about documents that can be downloaded from internet.
 *
 * @author Marc Plouhinec
 */
@Entity
@Table(name = "DOCUMENT")
class Document (
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "DOCUMENT_ID", nullable = false)
        var id: Long? = null,

        @Column(name = "DOCUMENT_URL", nullable = false, length = 2000)
        var url: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "DOCUMENT_TYPE", nullable = false)
        var type: DocumentType,

        @Column(name = "LANGUAGE_CODE", nullable = false, length = 20)
        var languageCode: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "PROVIDED_BY_AGENCY_ID", nullable = false)
        var providedByAgency: Agency,

        @Column(name = "REFERENCED_BY_DOCUMENT_TYPE", nullable = true)
        var referencedByDocumentType: DocumentType? = null,

        @Column(name = "IS_STILL_PROVIDED_BY_AGENCY", nullable = false)
        var isStillProvidedByAgency: Boolean = true

)