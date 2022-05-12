package com.example.shop

import assertk.assertThat
import assertk.assertions.*
import com.example.*
import com.example.plugins.*
import com.example.shop.ProductRepository.ProductAlreadyExistsException
import dev.forkhandles.fabrikate.Fabrikate
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.Before
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.*

internal class ShopTest {
    private val fabrikate = Fabrikate()

    @Before fun setup() = clearAllMocks()

    @Nested
    inner class ProductEndpoints() {
        private val productRepository: ProductRepository = mockk()

        @Test fun `empty shop returns empty product list`() {
            coEvery { productRepository.findAll() } returns emptyList()

            testApplication {
                application {
                    configureShop(productRepository = productRepository)
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

        @Test fun `returns not found on unknown product`() {
            coEvery { productRepository.find(any()) } returns null

            testApplication {
                application {
                    configureShop(productRepository = productRepository)
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
                    configureShop(productRepository = productRepository)
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
                    configureShop(productRepository = productRepository)
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
                    configureShop(productRepository = productRepository)
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
                    configureShop(productRepository = productRepository)
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
}
