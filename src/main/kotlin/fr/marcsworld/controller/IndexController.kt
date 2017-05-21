package fr.marcsworld.controller

import fr.marcsworld.service.AgencyService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
     *
     * @param includeRating If true, display rating on the page.
     */
    @RequestMapping("/")
    fun index(@RequestParam(name = "includeRating", required = false, defaultValue = "false") includeRating: Boolean): ModelAndView {
        val rootAgencyNode = agencyService.findAgencyTree(includeRating)

        val model = mapOf("rootAgencyNode" to rootAgencyNode, "includeRating" to includeRating)
        return ModelAndView("index", model)
    }
}