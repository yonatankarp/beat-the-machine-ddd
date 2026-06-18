package com.yonatankarp.beatthemachine.input.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

// Forwards SPA client-side routes under /app/** to the SPA entry point so deep
// links resolve. Scoped to /app/** so it never shadows the /api hierarchy,
// /actuator, or static resources. (The SPA itself ships in a later phase; until
// then /index.html is absent and these paths 404 harmlessly.)
@Controller
class SpaForwardingController {
    @RequestMapping("/app/**")
    fun forward(request: HttpServletRequest): String = "forward:/index.html"
}
