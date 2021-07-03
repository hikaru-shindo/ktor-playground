package com.example.shop

import assertk.assertThat
import assertk.assertions.*
import com.example.*
import com.example.plugins.*
import com.example.shop.ProductRepository.ProductAlreadyExistsException
import io.github.databob.Databob
import io.github.databob.Generators
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.junit.Before
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.*

internal class ShopTest {
    private val jsonFormat = Json { isLenient = true }
    private val databob = Databob(Generators.ofType { databob -> createProduct(databob) })

    @Before fun setup() = clearAllMocks()

    @Nested
    inner class ProductEndpoints() {
        private val productRepository: ProductRepository = mockk()

        @Test fun `empty shop returns empty product list`() {
            every { productRepository.findAll() } returns emptyList()

            withTestApplication({
                configureShop(productRepository = productRepository)
                configureSerialization()
            }) {
                handleRequest(HttpMethod.Get, "/product").apply {
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()
                    assertThat(response).hasValidJsonBody()

                    jsonFormat.decodeFromString<ProductListResponse>(response.content!!).apply {
                        assertThat(products).isEmpty()
                        assertThat(count).isEqualTo(0)
                    }
                }
            }
        }

        @Test fun `returns not found on unknown product`() {
            every { productRepository.find(any()) } returns null

            withTestApplication({
                configureShop(productRepository = productRepository)
                configureSerialization()
            }) {
                handleRequest(HttpMethod.Get, "/product/${UUID.randomUUID()}").apply {
                    assertThat(response).hasStatusNotFound()
                    assertThat(response.content).isNullOrEmpty()
                }
            }
        }

        @Test fun `product can be created`() {
            every { productRepository.add(any()) } just runs

            withTestApplication({
                configureShop(productRepository = productRepository)
                configureSerialization()
            }) {
                handleRequest(HttpMethod.Post, "/product") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(
                        """
                      {
                        "name": "foo",
                        "price": { "value": 13.21, "currencyCode": "EUR" }
                      }
                    """
                    )
                }.apply {
                    assertThat(response).hasStatusCreated()
                    assertThat(response).isJsonResponse()
                    assertThat(response).hasValidJsonBody()
                    jsonFormat.decodeFromString<Product>(response.content!!).apply {
                        assertThat(name).isEqualTo("foo")
                        assertThat(price.value).isEqualTo(13.21)
                        assertThat(price.currencyCode).isEqualTo("EUR")
                    }

                    verify(exactly = 1) { productRepository.add(any()) }
                }
            }
        }

        @Test fun `product can be retrieved`() {
            val testProduct = Product(
                id = UUID.randomUUID(),
                name = "foo",
                price = Price(
                    value = 47.11,
                    currencyCode = "EUR"
                )
            )
            every { productRepository.find(testProduct.id) } returns testProduct

            withTestApplication({
                configureShop(productRepository = productRepository)
                configureSerialization()
            }) {
                handleRequest(HttpMethod.Get, "/product/${testProduct.id}").apply {
                    assertThat(response).hasStatusOk()
                    assertThat(response).isJsonResponse()
                    assertThat(response).hasValidJsonBody()
                    jsonFormat.decodeFromString<Product>(response.content!!).apply {
                        assertThat(this).isDataClassEqualTo(testProduct)
                    }
                }
            }
        }

        @Test fun `product can be removed`() {
            val testUUID = UUID.randomUUID()

            every { productRepository.remove(testUUID) } just runs

            withTestApplication({
                configureShop(productRepository = productRepository)
                configureSerialization()
            }) {
                handleRequest(HttpMethod.Delete, "/product/$testUUID").apply {
                    assertThat(response).hasStatusNoContent()
                    assertThat(response.content).isNullOrEmpty()

                    verify(exactly = 1) { productRepository.remove(testUUID) }
                }
            }
        }

        @Test fun `product cannot be created twice`() {
            every { productRepository.add(any()) } throws ProductAlreadyExistsException(databob.mk())

            withTestApplication({
                configureShop(productRepository = productRepository)
                configureSerialization()
                configureErrorHandler()
            }) {
                handleRequest(HttpMethod.Post, "/product") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(
                        """
                      {
                        "name": "foo",
                        "price": { "value": 13.21, "currencyCode": "EUR" }
                      }
                    """
                    )
                }.apply {
                    assertThat(response).hasStatusConflict()

                    verify(exactly = 1) { productRepository.add(any()) }
                }
            }
        }
    }
}
