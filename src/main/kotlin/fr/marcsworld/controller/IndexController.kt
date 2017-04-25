package fr.marcsworld.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

/**
 * Controller for the homepage.
 *
 * @author Marc Plouhinec
 */
@Controller
open class IndexController {

    /**
     * Serves the application homepage.
     */
    @RequestMapping("/")
    fun index(): ModelAndView {
        return ModelAndView("index")
    }
}