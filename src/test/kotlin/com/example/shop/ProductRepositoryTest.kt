package com.example.shop

import assertk.assertThat
import assertk.assertions.*
import io.github.databob.Databob
import io.github.databob.Generators
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class ProductRepositoryTest {
    private val sut = ProductRepository()
    private val databob = Databob(Generators.ofType { databob -> createProduct(databob) })

    @Test
    fun `is initialized empty`() {
        assertThat(sut.findAll()).isEmpty()
    }

    @Test
    fun `can add product`() {
        val product = databob.mk<Product>()
        sut.add(product)

        sut.findAll().apply {
            assertThat(this).hasSize(1)
        }
    }

    @Test
    fun `cannot add same product twice`() {
        val product = databob.mk<Product>()
        sut.add(product)

        assertFailsWith<ProductRepository.ProductAlreadyExistsException> {
            sut.add(product)
        }
    }

    @Test
    fun `can add multiple products`() {
        val product1 = databob.mk<Product>()
        val product2 = databob.mk<Product>()
        sut.add(product1)
        sut.add(product2)

        sut.findAll().apply {
            assertThat(this).hasSize(2)
        }
    }

    @Test
    fun `can remove product`() {
        val product = databob.mk<Product>()
        sut.add(product)

        sut.remove(product.id)

        sut.findAll().apply {
            assertThat(this).doesNotContain(product)
        }
    }

    @Test
    fun `can find product by id`() {
        val product = databob.mk<Product>()
        sut.add(product)

        sut.find(product.id).apply {
            assertThat(this).isEqualTo(product)
        }
    }

    @Test
    fun `can return all products saved`() {
        val product1 = databob.mk<Product>()
        val product2 = databob.mk<Product>()
        val product3 = databob.mk<Product>()
        val product4 = databob.mk<Product>()
        sut.add(product1)
        sut.add(product2)
        sut.add(product3)
        sut.add(product4)

        sut.findAll().apply {
            assertThat(this).contains(product1)
            assertThat(this).contains(product2)
            assertThat(this).contains(product3)
            assertThat(this).contains(product4)
            assertThat(this).hasSize(4)
        }
    }
}
