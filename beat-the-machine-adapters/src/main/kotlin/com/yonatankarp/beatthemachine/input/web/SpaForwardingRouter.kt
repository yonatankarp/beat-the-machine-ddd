package com.yonatankarp.beatthemachine.input.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.net.URI

@Configuration
class SpaForwardingRouter {
    @Bean
    fun spaRoutes() =
        coRouter {
            GET("/") {
                ServerResponse
                    .status(HttpStatus.FOUND)
                    .location(URI.create("/app/"))
                    .buildAndAwait()
            }
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
