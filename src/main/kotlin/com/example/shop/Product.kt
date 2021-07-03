package com.example.shop

import com.example.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.lang.RuntimeException
import java.util.*

class ProductRepository {
    class ProductAlreadyExistsException(product: Product) :
        RuntimeException("Product with id ${product.id} already exists")

    private val products = mutableListOf<Product>()

    fun add(product: Product) {
        if (null != find(product.id)) {
            throw ProductAlreadyExistsException(product)
        }

        products.add(product)
    }
    fun find(id: UUID) = products.firstOrNull { id == it.id }?.copy()
    fun findAll() = products.toList()
    fun remove(id: UUID) {
        find(id)?.let {
            products.remove(it)
        }
    }
}

@Serializable
data class ProductListResponse(
    val products: List<Product>
) {
    val count: Int = products.count()
}

@Serializable
data class Product(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val price: Price
)

@Serializable
data class Price(val value: Double, val currencyCode: String)
