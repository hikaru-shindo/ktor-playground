package com.example

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.plugins.ErrorResponse
import com.example.plugins.configureErrorHandler
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

internal class ApplicationTest {
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)

    @BeforeEach fun setup() = clearAllMocks()

    @Test fun `root path returns hello world`() {
        testApplication {
            application { configureRouting(meterRegistry = meterRegistry) }

            client.get("/").let { response ->
                assertThat(response).hasStatusOk()
                assertThat(response.bodyAsText()).isEqualTo("Hello World!")
            }
        }
    }

    @Test fun `error route triggers an error`() {
        every {
            meterRegistry.counter(
                "error_endpoint_calls",
                "foo",
                "bar",
            ).increment()
        } just runs

        testApplication {
            application {
                configureRouting(meterRegistry = meterRegistry)
                configureErrorHandler()
                configureSerialization()
            }

            val client = createJsonClient()

            client.get("/error").let { response ->
                assertThat(response).hasStatusInternalServerError()
                assertThat(response).isJsonResponse()
                response.body<ErrorResponse>().apply {
                    assertThat(message).isEqualTo("this is a test")
                    assertThat(type).isEqualTo("Exception")
                }
            }

            verify(exactly = 1) {
                meterRegistry.counter(
                    "error_endpoint_calls",
                    "foo",
                    "bar",
                ).increment()
            }
        }
    }
}
