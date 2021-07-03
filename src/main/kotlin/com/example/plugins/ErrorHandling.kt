package com.example.plugins

import com.example.shop.ProductRepository
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.Serializable

fun Application.configureErrorHandler() {
    install(StatusPages) {
        exception<Throwable> { cause ->
            when (cause) {
                is ProductRepository.ProductAlreadyExistsException -> call.respond(
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
