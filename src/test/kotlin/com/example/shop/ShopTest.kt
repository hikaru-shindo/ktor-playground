package com.example.shop

import assertk.assertThat
import assertk.assertions.*
import com.example.*
import com.example.plugins.*
import com.example.shop.OrderRepository.OrderAlreadyExistsException
import com.example.shop.ProductRepository.ProductAlreadyExistsException
import dev.forkhandles.fabrikate.Fabrikate
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.*

internal class ShopTest {
    private val fabrikate = Fabrikate()

    @BeforeEach fun setup() = clearAllMocks()

    @Nested
    inner class ProductEndpoints() {
        private val productRepository: ProductRepository = mockk()

        @Test fun `empty shop returns empty product list`() {
            coEvery { productRepository.findAll() } returns emptyList()

            testApplication {
                application {
                    configureShop(productRepository = productRepository, orderRepository = mockk())
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/product").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<ProductListResponse>().apply {
                        assertThat(products).isEmpty()
                        assertThat(count).isEqualTo(0)
                    }
                }
            }
        }

        @Test fun `returns product list`() {
            val testProducts = arrayOf<Product>(
                fabrikate.random(),
                fabrikate.random()
            )

            coEvery { productRepository.findAll() } returns testProducts.toList()

            testApplication {
                application {
                    configureShop(productRepository = productRepository, orderRepository = mockk())
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/product").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<ProductListResponse>().apply {
                        assertThat(products).containsAtLeast(*testProducts)
                    }
                }
            }
        }

        @Test fun `returns not found on unknown product`() {
            coEvery { productRepository.find(any()) } returns null

            testApplication {
                application {
                    configureShop(productRepository = productRepository, orderRepository = mockk())
                    configureSerialization()
                }

                client.get("/product/${UUID.randomUUID()}").let { response ->
                    assertThat(response).hasStatusNotFound()
                    assertThat(response).hasEmptyBody()
                }
            }
        }

        @Test fun `product can be created`() {
            coEvery { productRepository.add(any()) } just runs

            testApplication {
                application {
                    configureShop(productRepository = productRepository, orderRepository = mockk())
                    configureSerialization()
                }

                val client = createJsonClient()

                val product = fabrikate.random<Product>()

                client.post("/product") {
                    contentType(ContentType.Application.Json)
                    setBody(product)
                }.let { response ->
                    assertThat(response).hasStatusCreated()
                    assertThat(response).isJsonResponse()

                    response.body<Product>().apply {
                        assertThat(name).isEqualTo(product.name)
                        assertThat(price.value).isEqualTo(product.price.value)
                        assertThat(price.currencyCode).isEqualTo(product.price.currencyCode)
                    }

                    coVerify(exactly = 1) { productRepository.add(any()) }
                }
            }
        }

        @Test fun `product can be retrieved`() {
            val product = fabrikate.random<Product>()

            coEvery { productRepository.find(product.id) } returns product

            testApplication {
                application {
                    configureShop(productRepository = productRepository, orderRepository = mockk())
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/product/${product.id}").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<Product>().apply {
                        assertThat(this).isDataClassEqualTo(product)
                    }
                }
            }
        }

        @Test fun `product can be removed`() {
            val testUUID = UUID.randomUUID()

            coEvery { productRepository.remove(testUUID) } just runs

            testApplication {
                application {
                    configureShop(productRepository = productRepository, orderRepository = mockk())
                    configureSerialization()
                }

                client.delete("/product/$testUUID").let { response ->
                    assertThat(response).hasStatusNoContent()
                    assertThat(response).hasEmptyBody()

                    coVerify(exactly = 1) { productRepository.remove(testUUID) }
                }
            }
        }

        @Test fun `product cannot be created twice`() {
            coEvery { productRepository.add(any()) } throws ProductAlreadyExistsException(fabrikate.random())

            testApplication {
                application {
                    configureShop(productRepository = productRepository, orderRepository = mockk())
                    configureSerialization()
                    configureErrorHandler()
                }

                val client = createJsonClient()

                client.post("/product") {
                    contentType(ContentType.Application.Json)
                    setBody(fabrikate.random<Product>())
                }.let { response ->
                    assertThat(response).hasStatusConflict()

                    coVerify(exactly = 1) { productRepository.add(any()) }
                }
            }
        }
    }

    @Nested
    inner class OrderEndpoints {
        private val orderRepository: OrderRepository = mockk()

        @Test fun `empty shop returns empty order list`() {
            coEvery { orderRepository.findAll() } returns emptyList()

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/order").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<OrderListResponse>().apply {
                        assertThat(orders).isEmpty()
                        assertThat(count).isEqualTo(0)
                    }
                }
            }
        }

        @Test fun `returns order list`() {
            val testOrders = arrayOf<Order>(
                fabrikate.random(),
                fabrikate.random()
            )

            coEvery { orderRepository.findAll() } returns testOrders.toList()

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/order").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<OrderListResponse>().apply {
                        assertThat(orders).containsAtLeast(*testOrders)
                    }
                }
            }
        }

        @Test fun `returns not found on unknown order`() {
            coEvery { orderRepository.find(any()) } returns null

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                }

                client.get("/order/${UUID.randomUUID()}").let { response ->
                    assertThat(response).hasStatusNotFound()
                    assertThat(response).hasEmptyBody()
                }
            }
        }

        @Test fun `order can be created`() {
            coEvery { orderRepository.add(any()) } just runs

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                }

                val client = createJsonClient()

                val order = fabrikate.random<Order>()

                client.post("/order") {
                    contentType(ContentType.Application.Json)
                    setBody(order)
                }.let { response ->
                    assertThat(response).hasStatusCreated()
                    assertThat(response).isJsonResponse()

                    response.body<Order>().apply {
                        assertThat(customer.customerNumber)
                        assertThat(customer.name)
                        assertThat(customer.address)

                        assertThat(products).hasSize(order.products.size)
                        assertThat(products).containsAtLeast(*order.products.toTypedArray())
                    }

                    coVerify(exactly = 1) { orderRepository.add(any()) }
                }
            }
        }

        @Test fun `order can be retrieved`() {
            val order = fabrikate.random<Order>()

            coEvery { orderRepository.find(order.id) } returns order

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/order/${order.id}").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<Order>().apply {
                        assertThat(this).isDataClassEqualTo(order)
                    }
                }
            }
        }

        @Test fun `order cannot be created twice`() {
            coEvery { orderRepository.add(any()) } throws OrderAlreadyExistsException(fabrikate.random())

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                    configureErrorHandler()
                }

                val client = createJsonClient()

                client.post("/order") {
                    contentType(ContentType.Application.Json)
                    setBody(fabrikate.random<Order>())
                }.let { response ->
                    assertThat(response).hasStatusConflict()

                    coVerify(exactly = 1) { orderRepository.add(any()) }
                }
            }
        }

        @Test
        fun `orders can be retrieved by customer`() {
            val testCustomer = fabrikate.random<Order.Customer>()
            val testOrders = arrayOf(
                fabrikate.random<Order>().copy(customer = testCustomer),
                fabrikate.random<Order>().copy(customer = testCustomer)
            )

            coEvery { orderRepository.findByCustomer(any()) } returns testOrders.toList()

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/customer/${testCustomer.customerNumber}/order").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<OrderListResponse>().apply {
                        assertThat(orders).containsAtLeast(*testOrders)
                    }
                }
            }
        }

        @Test
        fun `orders can be retrieved for unknown customer`() {
            coEvery { orderRepository.findByCustomer(any()) } returns emptyList()

            testApplication {
                application {
                    configureShop(productRepository = mockk(), orderRepository = orderRepository)
                    configureSerialization()
                }

                val client = createJsonClient()

                client.get("/customer/unknwonCustomer/order").let { response ->
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()

                    response.body<OrderListResponse>().apply {
                        assertThat(orders).isEmpty()
                    }
                }
            }
        }
    }
}
