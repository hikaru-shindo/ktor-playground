package com.example

import com.example.plugins.configureErrorHandler
import com.example.plugins.configureHTTP
import com.example.plugins.configureHealthChecks
import com.example.plugins.configureMonitoring
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.shop.ProductRepository
import com.example.shop.configureShop
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT).also { registry ->
            registry.config().commonTags(
                "application", "ktor-playground",
                "squad", "foo",
            )
        }

        configureRouting(meterRegistry = prometheusMeterRegistry)
        configureShop(productRepository = ProductRepository())
        configureHealthChecks()
        configureErrorHandler()
        configureHTTP(enabledIpForwarding = !developmentMode, allowedCORSHosts = emptySet())
        configureMonitoring(meterRegistry = prometheusMeterRegistry)
        configureSerialization()
    }.start(wait = true)
}
