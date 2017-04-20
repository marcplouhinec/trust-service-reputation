package fr.marcsworld.model

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
        @Id
        @Column(name = "DOCUMENT_URL", nullable = false, length = 2000)
        var url: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "DOCUMENT_TYPE", nullable = false)
        var type: DocumentType,

        @Column(name = "LANGUAGE_CODE", nullable = false, length = 20)
        var languageCode: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "PROVIDER_AGENCY_ID", nullable = false)
        var providerAgency: Agency
)