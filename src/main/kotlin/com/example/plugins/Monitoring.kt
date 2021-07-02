package com.example.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.prometheus.*
import org.slf4j.event.*
import java.util.*
import java.util.regex.Pattern

fun Application.configureMonitoring(
    meterRegistry: PrometheusMeterRegistry,
    metricsEndpoint: String = "/internal/metrics"
) {
    configureRequestLogging()
    configurePrometheusMetrics(meterRegistry, metricsEndpoint)
}

private fun Application.configureRequestLogging() {
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString() }
        verify {
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$")
                .matcher(it)
                .matches()
        }
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc("callId")
    }
}

private fun Application.configurePrometheusMetrics(meterRegistry: PrometheusMeterRegistry, metricsEndpoint: String) {
    install(MicrometerMetrics) {
        registry = meterRegistry
    }

    routing {
        get(metricsEndpoint) {
            call.respond(meterRegistry.scrape())
        }
    }
}
