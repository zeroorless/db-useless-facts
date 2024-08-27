package utils

import gp.example.utils.Base62Encoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Base62EncoderTest {

    private val encoder = Base62Encoder()

    @Test
    fun testEncodeZero() {
        assertEquals("0", encoder.encode(0L), "Encoding 0 should return '0'")
    }

    @Test
    fun testEncodeSingleDigit() {
        assertEquals("5", encoder.encode(5L), "Encoding 5 should return '5'")
        assertEquals("Z", encoder.encode(35L), "Encoding 35 should return 'Z'")
        assertEquals("z", encoder.encode(61L), "Encoding 61 should return 'z'")
    }

    @Test
    fun testEncodeWithCarryOver() {
        assertEquals("10", encoder.encode(62L), "Encoding 62 should return '10'")
        assertEquals("Ju", encoder.encode(1234L), "Encoding 1234 should return 'Ju'")
    }

    @Test
    fun testEncodeMaxLong() {
        assertEquals("AzL8n0Y58m7", encoder.encode(Long.MAX_VALUE), "Encoding Long.MAX_VALUE should return 'AzL8n0Y58m7'")
    }

    @Test
    fun testEncodeNegativeNumberThrowsException() {
        assertFailsWith<IllegalArgumentException> { encoder.encode(-1L) }
    }
}