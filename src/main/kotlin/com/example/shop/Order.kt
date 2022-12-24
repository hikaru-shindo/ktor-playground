package com.example.shop

import com.example.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.lang.RuntimeException
import java.util.*

class OrderRepository {

    class OrderAlreadyExistsException(order: Order) :
        RuntimeException("Order with id ${order.id} already exists")

    private val orders = mutableListOf<Order>()

    suspend fun add(order: Order) {
        if (orders.any { existingOrder -> existingOrder.id == order.id }) {
            throw OrderAlreadyExistsException(order)
        }

        orders.add(order)
    }

    suspend fun find(id: UUID) = orders.firstOrNull { order ->
        id == order.id
    }?.copy()

    suspend fun findAll() = orders.toList().map { order ->
        order.copy()
    }

    suspend fun findByCustomer(customerNumber: String) = orders.filter { order ->
        customerNumber == order.customer.customerNumber
    }.map { order ->
        order.copy()
    }
}

@Serializable
data class OrderListResponse(
    val orders: List<Order>
) {
    val count: Int = orders.count()
}

@Serializable
data class Order(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val customer: Customer,
    val products: List<Product>
) {
    @Serializable
    data class Product(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val name: String,
        val price: Price,
        val quantity: Int
    ) {
        @Serializable
        data class Price(val value: Double, val currencyCode: String)
    }

    @Serializable
    data class Customer(val customerNumber: String, val name: String, val address: String)
}
