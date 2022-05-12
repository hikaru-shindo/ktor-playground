package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*

fun Application.configureHTTP(allowedCORSHosts: Set<String>, enabledIpForwarding: Boolean) {
    configureCORS(allowedCORSHosts)

    if (enabledIpForwarding) {
        configureReverseProxy()
    }
}

private fun Application.configureReverseProxy() {
    install(ForwardedHeaders)
    install(XForwardedHeaders)
}

private fun Application.configureCORS(allowedCORSHosts: Set<String>) {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)

        if (allowedCORSHosts.isEmpty()) {
            anyHost()
        } else {
            allowCredentials = true
            hosts.addAll(allowedCORSHosts)
        }
    }
}
