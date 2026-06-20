package com.yonatankarp.beatthemachine.input.web

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SecurityHeadersFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        exchange.response.beforeCommit {
            with(exchange.response.headers) {
                set("Content-Security-Policy", CSP)
                set("X-Content-Type-Options", "nosniff")
                set("Referrer-Policy", "no-referrer")
            }
            Mono.empty()
        }
        return chain.filter(exchange)
    }

    private companion object {
        const val CSP =
            "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' https: data:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "object-src 'none'; " +
                "base-uri 'self'; " +
                "frame-ancestors 'none'"
    }
}
