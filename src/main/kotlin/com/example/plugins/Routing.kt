package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.MeterRegistry

fun Application.configureRouting(meterRegistry: MeterRegistry) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/error") {
            meterRegistry.counter(
                "error_endpoint_calls",
                "foo", "bar",
            ).increment()
            throw Exception("this is a test")
        }
    }
}
