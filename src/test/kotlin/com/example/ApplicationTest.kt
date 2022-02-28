package com.example

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.plugins.ErrorResponse
import com.example.plugins.configureErrorHandler
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.http.*
import io.ktor.server.testing.*
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.junit.Before
import kotlin.test.*

internal class ApplicationTest {
    private val jsonFormat = Json { isLenient = true }
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)

    @Before fun setup() = clearAllMocks()

    @Test fun `root path returns hello world`() {
        withTestApplication({ configureRouting(meterRegistry = meterRegistry) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(response).hasStatusOk()
                assertThat(response.content).isEqualTo("Hello World!")
            }
        }
    }

    @Test fun `error route triggers an error`() {
        every {
            meterRegistry.counter(
                "error_endpoint_calls",
                "foo", "bar",
            ).increment()
        } just runs

        withTestApplication({
            configureRouting(meterRegistry = meterRegistry)
            configureErrorHandler()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/error").apply {
                assertThat(response).hasStatusInternalServerError()
                assertThat(response).hasValidJsonBody()
                jsonFormat.decodeFromString<ErrorResponse>(response.content!!).apply {
                    assertThat(message).isEqualTo("this is a test")
                    assertThat(type).isEqualTo("Exception")
                }
            }

            verify(exactly = 1) {
                meterRegistry.counter(
                    "error_endpoint_calls",
                    "foo", "bar",
                ).increment()
            }
        }
    }
}
