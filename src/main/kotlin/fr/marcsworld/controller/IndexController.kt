package fr.marcsworld.controller

import fr.marcsworld.service.AgencyService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

/**
 * Controller for the homepage.
 *
 * @author Marc Plouhinec
 */
@Controller
open class IndexController(
        val agencyService: AgencyService
) {

    /**
     * Serves the application homepage.
     */
    @RequestMapping("/")
    fun index(): ModelAndView {
        val rootAgencyNode = agencyService.findAgencyTree()

        val model = mapOf("rootAgencyNode" to rootAgencyNode)
        return ModelAndView("index", model)
    }
}