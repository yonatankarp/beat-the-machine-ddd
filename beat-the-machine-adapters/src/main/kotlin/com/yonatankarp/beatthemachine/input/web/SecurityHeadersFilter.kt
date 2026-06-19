package com.yonatankarp.beatthemachine.input.web

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

// Adds defence-in-depth security headers to every response. The SPA is served
// same-origin by this backend, so the policy is restrictive: scripts only from
// self, the AI image host allowed over https (tighten to the concrete host once
// image generation is wired), inline styles permitted for the motion runtime,
// and framing denied.
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
                "img-src 'self' https:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "object-src 'none'; " +
                "base-uri 'self'; " +
                "frame-ancestors 'none'"
    }
}
