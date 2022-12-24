package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import java.lang.RuntimeException

fun Application.configureErrorHandler() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is HttpConflictException -> call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse.fromException(cause)
                )
                else -> call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse.fromException(cause)
                )
            }

            call.application.log.error("Unhandled error occured", cause)
        }
    }
}

open class HttpConflictException(cause: Throwable? = null) : RuntimeException(cause)

@Serializable
data class ErrorResponse(
    val message: String,
    val type: String
) {
    companion object {
        fun fromException(exception: Throwable) = ErrorResponse(
            message = exception.message ?: "unknown error",
            type = exception.javaClass.simpleName
        )
    }
}
