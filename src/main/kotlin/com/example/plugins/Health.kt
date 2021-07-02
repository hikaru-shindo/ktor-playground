package com.example.plugins

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureHealthChecks(healthEndpoint: String = "/internal/health") {
    routing {
        route(healthEndpoint) {
            get("/liveness") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("status" to "UP", "groups" to arrayOf("liveness"))
                )
            }
            get("/readiness") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("status" to "UP", "groups" to arrayOf("readiness"))
                )
            }
            get {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("status" to "UP", "groups" to arrayOf("liveness", "readiness"))
                )
            }
        }
    }
}
