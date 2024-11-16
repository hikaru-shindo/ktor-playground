package com.example.shop

import com.example.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.lang.RuntimeException
import java.util.*

class ProductRepository {
    class ProductAlreadyExistsException(product: Product) :
        RuntimeException("Product with id ${product.id} already exists")

    private val products = mutableListOf<Product>()

    suspend fun add(product: Product) {
        if (products.any { existingProduct -> existingProduct.id == product.id }) {
            throw ProductAlreadyExistsException(product)
        }

        products.add(product)
    }

    suspend fun find(id: UUID) = products.firstOrNull { product ->
        id == product.id
    }?.copy()

    suspend fun findAll() = products.toList().map { product ->
        product.copy()
    }

    suspend fun remove(id: UUID) {
        products.firstOrNull { product ->
            id == product.id
        }?.also { product ->
            products.remove(product)
        }
    }
}

@Serializable
data class ProductListResponse(
    val products: List<Product>,
) {
    val count: Int = products.count()
}

@Serializable
data class Product(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val price: Price,
) {
    @Serializable
    data class Price(val value: Double, val currencyCode: String)
}
