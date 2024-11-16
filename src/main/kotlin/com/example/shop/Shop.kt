package com.example.shop

import com.example.plugins.HttpConflictException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.util.*

fun Application.configureShop(productRepository: ProductRepository, orderRepository: OrderRepository) {
    routing {
        route("/product") {
            get {
                productRepository.findAll().let { productList ->
                    call.respond(ProductListResponse(productList))
                }
            }

            post {
                call.receive<Product>().let { product ->
                    try {
                        productRepository.add(product = product)
                    } catch (exception: ProductRepository.ProductAlreadyExistsException) {
                        throw HttpConflictException(exception)
                    }

                    call.respond(HttpStatusCode.Created, product)
                }
            }

            route("/{id}") {
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

        route("/order") {
            get {
                orderRepository.findAll().let { orderList ->
                    call.respond(OrderListResponse(orderList))
                }
            }

            post {
                call.receive<Order>().let { order ->
                    try {
                        orderRepository.add(order = order)
                    } catch (exception: OrderRepository.OrderAlreadyExistsException) {
                        throw HttpConflictException(exception)
                    }

                    call.respond(HttpStatusCode.Created, order)
                }
            }

            get("/{id}") {
                orderRepository.find(UUID.fromString(call.parameters["id"]))?.let { order ->
                    call.respond(order)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/customer/{customerNumber}/order") {
            orderRepository.findByCustomer(call.parameters.getOrFail("customerNumber")).let { orderList ->
                call.respond(OrderListResponse(orderList))
            }
        }
    }
}
