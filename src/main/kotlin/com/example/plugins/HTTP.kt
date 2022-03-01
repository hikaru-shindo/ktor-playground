package com.example.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configureHTTP(allowedCORSHosts: Set<String>, enabledIpForwarding: Boolean) {
    configureCORS(allowedCORSHosts)

    if (enabledIpForwarding) {
        configureReverseProxy()
    }
}

private fun Application.configureReverseProxy() {
    install(ForwardedHeaderSupport)
    install(XForwardedHeaderSupport)
}

private fun Application.configureCORS(allowedCORSHosts: Set<String>) {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)

        if (allowedCORSHosts.isEmpty()) {
            anyHost()
        } else {
            allowCredentials = true
            hosts.addAll(allowedCORSHosts)
        }
    }
}
