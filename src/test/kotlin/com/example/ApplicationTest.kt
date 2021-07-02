package com.example

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.plugins.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import org.junit.Before
import kotlin.test.*

class ApplicationTest {
    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)

    @Before fun setup() = clearAllMocks()

    @Test fun `root path returns hello world`() {
        withTestApplication({ configureRouting(meterRegistry = meterRegistry) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello World!", response.content)
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
                assertThat(response.status()).isEqualTo(HttpStatusCode.InternalServerError)
                assertThat(response.content).contains("this is a test")
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
