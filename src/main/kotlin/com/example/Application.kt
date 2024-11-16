package com.example

import com.example.plugins.configureErrorHandler
import com.example.plugins.configureHTTP
import com.example.plugins.configureHealthChecks
import com.example.plugins.configureMonitoring
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.shop.OrderRepository
import com.example.shop.ProductRepository
import com.example.shop.configureShop
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT).also { registry ->
        registry.config().commonTags(
            "application",
            "ktor-playground",
            "squad",
            "foo"
        )
    }

    configureRouting(meterRegistry = prometheusMeterRegistry)
    configureShop(productRepository = ProductRepository(), orderRepository = OrderRepository())
    configureHealthChecks()
    configureErrorHandler()
    configureHTTP(enabledIpForwarding = !developmentMode, allowedCORSHosts = emptySet())
    configureMonitoring(meterRegistry = prometheusMeterRegistry)
    configureSerialization()
}
