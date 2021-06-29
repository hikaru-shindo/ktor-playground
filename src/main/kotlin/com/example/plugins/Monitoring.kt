package com.example.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.prometheus.*
import org.slf4j.event.*

val Application.micrometerRegistry: PrometheusMeterRegistry by lazy {
    PrometheusMeterRegistry(PrometheusConfig.DEFAULT).also { registry ->
        registry.config().commonTags(
            "application", "ktor-playground",
            "squad", "foo",
        )
    }
}

fun Application.configureMonitoring() {
    install(MicrometerMetrics) {
        registry = micrometerRegistry
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    routing {
        get("/internal/metrics") {
            call.respond(micrometerRegistry.scrape())
        }
    }
}
