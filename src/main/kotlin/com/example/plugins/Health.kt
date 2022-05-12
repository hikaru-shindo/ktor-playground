package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.text.get

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
