package gp.example.utils

class Base62Encoder : Encoder {
    companion object {
        private val alphabet = ('0' .. '9').toList() + ('A'..'Z').toList() + ('a'..'z').toList()
        private val base = 62
        const val zero = "0"
    }

    /**
     * Encodes a long number to a base-62 number
     * @param n non-negative long number
     * @return Base-62 encoded string
     * @throws IllegalArgumentException if n is negative.
     */
    override fun encode(n: Long): String {
        if (n < 0L) throw IllegalArgumentException("n must be non-negative")
        return if (n == 0L) zero
        else {
            var n = n
            val accumulator = StringBuilder()
            while (n > 0L) {
                val c = alphabet[(n % base).toInt()]
                accumulator.append(c)
                n = n / base
            }
            accumulator.reverse().toString()
        }
    }
}