package com.example.serialization

import assertk.assertThat
import assertk.assertions.*
import io.mockk.*
import kotlinx.serialization.encoding.*
import java.lang.IllegalArgumentException
import java.util.UUID
import kotlin.test.*

class UUIDSerializerTest {
    private val encoder = mockk<Encoder>()
    private val decoder = mockk<Decoder>()

    @Test
    fun `encodes valid UUID`() {
        every { encoder.encodeString(any()) } just runs
        val uuid = UUID.randomUUID()

        UUIDSerializer.serialize(encoder, uuid)

        verify(exactly = 1) { encoder.encodeString(uuid.toString()) }
    }

    @Test
    fun `decodes valid UUID`() {
        val uuid = UUID.randomUUID()
        every { decoder.decodeString() } returns uuid.toString()

        UUIDSerializer.deserialize(decoder).apply {
            assertThat(this).isEqualTo(uuid)
        }
    }

    @Test
    fun `fails on decoding invalid UUID`() {
        every { decoder.decodeString() } returns "foo"

        assertFailsWith<IllegalArgumentException> {
            UUIDSerializer.deserialize(decoder)
        }
    }
}
