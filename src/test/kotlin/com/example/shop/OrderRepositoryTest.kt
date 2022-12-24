package com.example.shop

import assertk.assertThat
import assertk.assertions.*
import dev.forkhandles.fabrikate.Fabrikate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class OrderRepositoryTest {
    private val sut = OrderRepository()
    private val fabrikate = Fabrikate()

    @Test
    fun `is initialized empty`() = runTest {
        assertThat(sut.findAll()).isEmpty()
    }

    @Test
    fun `can add order`() = runTest {
        val order = fabrikate.random<Order>()
        sut.add(order)

        sut.findAll().apply {
            assertThat(this).contains(order)
        }
    }

    @Test
    fun `cannot add same order twice`() = runTest {
        val order = fabrikate.random<Order>()
        sut.add(order)

        assertFailsWith<OrderRepository.OrderAlreadyExistsException> {
            sut.add(order)
        }
    }

    @Test
    fun `can add multiple orders`() = runTest {
        val testOrders = arrayOf<Order>(
            fabrikate.random(),
            fabrikate.random()
        )

        testOrders.forEach { testOrder ->
            sut.add(testOrder)
        }

        sut.findAll().apply {
            assertThat(this).containsAll(*testOrders)
        }
    }

    @Test
    fun `can find order by id`() = runTest {
        val order = fabrikate.random<Order>()
        sut.add(order)

        sut.find(order.id).apply {
            assertThat(this).isEqualTo(order)
            assertThat(this).isNotSameAs(order)
        }
    }

    @Test
    fun `can return all orders saved`() = runTest {
        val testOrders = arrayOf<Order>(
            fabrikate.random(),
            fabrikate.random(),
            fabrikate.random(),
            fabrikate.random()
        )

        testOrders.forEach { testOrder ->
            sut.add(testOrder)
        }

        sut.findAll().apply {
            assertThat(this).containsExactlyInAnyOrder(*testOrders)
            testOrders.forEach { testOrder ->
                assertThat(this.any { order -> testOrder === order }).isFalse()
            }
        }
    }

    @Test
    fun `can find orders by customer`() = runTest {
        val customer = fabrikate.random<Order.Customer>()
        val customerOrders = arrayOf(
            fabrikate.random<Order>().copy(customer = customer),
            fabrikate.random<Order>().copy(customer = customer)
        )
        val testOrders = arrayOf(
            fabrikate.random(),
            fabrikate.random(),
            *customerOrders
        )

        testOrders.forEach { testOrder ->
            sut.add(testOrder)
        }

        sut.findByCustomer(customer.customerNumber).apply {
            assertThat(this).containsExactlyInAnyOrder(*customerOrders)
            customerOrders.forEach { customerOrder ->
                assertThat(this.any { order -> customerOrder === order }).isFalse()
            }
        }
    }

    @Test
    fun `unknown customer returns empty list`() = runTest {
        val testOrders = arrayOf<Order>(
            fabrikate.random(),
            fabrikate.random()
        )

        testOrders.forEach { testOrder ->
            sut.add(testOrder)
        }

        sut.findByCustomer("unknown").apply {
            assertThat(this).isEmpty()
        }
    }
}
