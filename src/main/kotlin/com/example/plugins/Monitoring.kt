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

    routing {
        get("/internal/metrics") {
            call.respond(micrometerRegistry.scrape())
        }
    }
}
