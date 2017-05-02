package fr.marcsworld.model.dto

import fr.marcsworld.model.entity.Agency
import fr.marcsworld.model.entity.AgencyName

/**
 * Tree node representing an agency and its children.
 *
 * @author Marc Plouhinec
 */
class AgencyNode (
        val agency: Agency,

        val mainAgencyName: AgencyName,

        val active: Boolean,

        val documentNodes: List<DocumentNode>,

        val childrenAgencyNodes: List<AgencyNode>
)