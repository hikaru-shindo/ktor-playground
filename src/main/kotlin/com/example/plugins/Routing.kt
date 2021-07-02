package com.example.plugins

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
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
