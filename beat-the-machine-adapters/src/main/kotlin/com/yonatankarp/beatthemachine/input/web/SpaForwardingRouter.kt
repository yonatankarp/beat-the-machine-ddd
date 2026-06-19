package com.yonatankarp.beatthemachine.input.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

// Forwards SPA client-side routes under /app/** to the SPA entry point so deep
// links resolve. Scoped to /app/** so it never shadows the /api hierarchy,
// /actuator, or static resources. (The SPA itself ships in a later phase; until
// then static/index.html is absent and these paths 404 harmlessly.)
@Configuration
class SpaForwardingRouter {
    @Bean
    fun spaRoutes() =
        coRouter {
            GET("/app/**") {
                val index = ClassPathResource("static/index.html")
                if (index.exists()) {
                    ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValueAndAwait(index)
                } else {
                    ServerResponse.notFound().buildAndAwait()
                }
            }
        }
}
