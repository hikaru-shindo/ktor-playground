package com.example

import assertk.Assert
import assertk.assertions.support.expected
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun Assert<String?>.contains(expected: CharSequence) = given { actual ->
    if (actual?.contains(expected) == true) expected("to contain $expected")
}

fun Assert<HttpResponse>.hasStatus(expected: HttpStatusCode) = given { actual ->
    if (actual.status != expected) expected("status to be $expected but is ${actual.status}")
}

fun Assert<HttpResponse>.hasStatusOk() = hasStatus(HttpStatusCode.OK)
fun Assert<HttpResponse>.hasStatusNotFound() = hasStatus(HttpStatusCode.NotFound)
fun Assert<HttpResponse>.hasStatusNoContent() = hasStatus(HttpStatusCode.NoContent)
fun Assert<HttpResponse>.hasStatusCreated() = hasStatus(HttpStatusCode.Created)
fun Assert<HttpResponse>.hasStatusInternalServerError() = hasStatus(HttpStatusCode.InternalServerError)
fun Assert<HttpResponse>.hasStatusConflict() = hasStatus(HttpStatusCode.Conflict)

fun Assert<HttpResponse>.hasEmptyBody() = runBlocking {
    given { actual ->
        if (actual.bodyAsText().isNotEmpty()) {
            expected("to be empty but is not")
        }
    }
}

fun Assert<HttpResponse>.isJsonResponse() = given { actual ->
    if (false == actual.contentType()?.match(ContentType.Application.Json)) {
        expected("to have ${ContentType.Application.Json} content type, but is ${actual.contentType()}")
    }
}
