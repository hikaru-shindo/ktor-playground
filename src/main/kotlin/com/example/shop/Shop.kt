package com.example.shop

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

fun Application.configureShop(productRepository: ProductRepository) {
    routing {
        route("/product") {
            get() {
                productRepository.findAll().let { productList ->
                    call.respond(ProductListResponse(productList))
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
