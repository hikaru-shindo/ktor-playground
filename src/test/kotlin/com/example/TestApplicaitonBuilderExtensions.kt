package com.example

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*

fun ApplicationTestBuilder.createJsonClient() = createClient {
    install(ContentNegotiation) {
        json()
    }
}
