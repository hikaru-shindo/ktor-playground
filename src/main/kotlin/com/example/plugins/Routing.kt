package com.example.plugins

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/error") {
            call.application.micrometerRegistry.counter(
                "error_endpoint_calls",
                "foo", "bar",
            ).increment()
            throw Exception("this is a test")
        }
    }
}
