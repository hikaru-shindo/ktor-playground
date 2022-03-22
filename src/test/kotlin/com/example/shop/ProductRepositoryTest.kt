package com.example.shop

import assertk.assertThat
import assertk.assertions.*
import dev.forkhandles.fabrikate.Fabrikate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class ProductRepositoryTest {
    private val sut = ProductRepository()
    private val fabrikate = Fabrikate()

    @Test
    fun `is initialized empty`() = runTest {
        assertThat(sut.findAll()).isEmpty()
    }

    @Test
    fun `can add product`() = runTest {
        val product = fabrikate.random<Product>()
        sut.add(product)

        sut.findAll().apply {
            assertThat(this).hasSize(1)
        }
    }

    @Test
    fun `cannot add same product twice`() = runTest {
        val product = fabrikate.random<Product>()
        sut.add(product)

        assertFailsWith<ProductRepository.ProductAlreadyExistsException> {
            sut.add(product)
        }
    }

    @Test
    fun `can add multiple products`() = runTest {
        val product1 = fabrikate.random<Product>()
        val product2 = fabrikate.random<Product>()
        sut.add(product1)
        sut.add(product2)

        sut.findAll().apply {
            assertThat(this).hasSize(2)
        }
    }

    @Test
    fun `can remove product`() = runTest {
        val product = fabrikate.random<Product>()
        sut.add(product)

        sut.remove(product.id)

        sut.findAll().apply {
            assertThat(this).doesNotContain(product)
        }
    }

    @Test
    fun `can find product by id`() = runTest {
        val product = fabrikate.random<Product>()
        sut.add(product)

        sut.find(product.id).apply {
            assertThat(this).isEqualTo(product)
        }
    }

    @Test
    fun `can return all products saved`() = runTest {
        val product1 = fabrikate.random<Product>()
        val product2 = fabrikate.random<Product>()
        val product3 = fabrikate.random<Product>()
        val product4 = fabrikate.random<Product>()
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
