package com.example.plugins

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.core.instrument.MeterRegistry
import java.util.*

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

    val productRepository = ProductRepository()
    routing {
        route("/product") {
            get() {
                productRepository.findAll().let { productList ->
                    call.respond(mapOf("products" to productList, "count" to productList.count()))
                }
            }

            post {
                call.receive<Product>().let { product ->
                    productRepository.add(product = product)

                    call.respond(HttpStatusCode.Created, product)
                }
            }
        }

        route("/product/{id}") {
            delete {
                productRepository.remove(UUID.fromString(call.parameters["id"]))

                call.respond(HttpStatusCode.NoContent)
            }

            get {
                productRepository.find(UUID.fromString(call.parameters["id"]))?.let { product ->
                    call.respond(product)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

class ProductRepository {
    private val products = mutableListOf<Product>()

    fun add(product: Product) { products.add(product) }
    fun find(id: UUID) = products.firstOrNull { id == it.id }?.copy()
    fun findAll() = products.toList()
    fun remove(id: UUID) {
        find(id)?.let {
            products.remove(it)
        }
    }
}

data class Product(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val price: Price
)

data class Price(val value: Float, val currencyCode: String)
