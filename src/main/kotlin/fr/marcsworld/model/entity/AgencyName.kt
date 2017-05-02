package fr.marcsworld.model.entity

import javax.persistence.*

/**
 * Represents the name of an agency translated into a language.
 *
 * @author Marc Plouhinec
 */
@Entity
@Table(name = "AGENCY_NAME")
class AgencyName (
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "AGENCY_NAME_ID", nullable = false)
        var id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "AGENCY_ID", nullable = false)
        var agency: Agency,

        @Column(name = "NAME", nullable = false, length = 500)
        var name: String,

        @Column(name = "LANGUAGE_CODE", nullable = false, length = 20)
        var languageCode: String
)