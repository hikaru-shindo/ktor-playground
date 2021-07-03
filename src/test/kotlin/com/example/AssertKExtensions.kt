package com.example

import assertk.Assert
import assertk.assertions.support.expected
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun Assert<String?>.contains(expected: CharSequence) = given { actual ->
    if (actual?.contains(expected) == true) expected("to contain $expected")
}

fun Assert<TestApplicationResponse>.hasStatus(expected: HttpStatusCode) = given { actual ->
    if (actual.status() != expected) expected("status to be $expected but is ${actual.status()}")
}

fun Assert<TestApplicationResponse>.hasStatusOk() = hasStatus(HttpStatusCode.OK)
fun Assert<TestApplicationResponse>.hasStatusNotFound() = hasStatus(HttpStatusCode.NotFound)
fun Assert<TestApplicationResponse>.hasStatusNoContent() = hasStatus(HttpStatusCode.NoContent)
fun Assert<TestApplicationResponse>.hasStatusCreated() = hasStatus(HttpStatusCode.Created)
fun Assert<TestApplicationResponse>.hasStatusInternalServerError() = hasStatus(HttpStatusCode.InternalServerError)
fun Assert<TestApplicationResponse>.hasStatusConflict() = hasStatus(HttpStatusCode.Conflict)

fun Assert<TestApplicationResponse>.isJsonResponse() = given { actual ->
    if (! actual.contentType().match(ContentType.Application.Json))
        expected("to have ${ContentType.Application.Json} content type, but is ${actual.contentType()}")
}

fun Assert<TestApplicationResponse>.hasValidJsonBody() = given { actual ->
    actual.content?.let { actualContent ->
        try {
            Json.parseToJsonElement(actualContent)
        } catch (exception: SerializationException) {
            expected("to be valid json content")
        }
    } ?: expected("content to not be null")
}
