package com.yonatankarp.beatthemachine.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

// Forwards SPA routes that start with /app/ to /index.html.
// Scope is intentionally narrow to avoid shadowing legacy routes (/, /index, /index.html,
// favicon.ico, /{id}/guess, etc.), the /api hierarchy, /actuator, and static resources.
// A broader catch-all is safe once legacy controllers are removed in Phase 5.
@Controller
class SpaForwardingController {
    @RequestMapping("/app/**")
    fun forward(request: HttpServletRequest): String = "forward:/index.html"
}
