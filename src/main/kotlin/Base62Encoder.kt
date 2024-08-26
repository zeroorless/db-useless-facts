package gp.example

class Base62Encoder : Encoder {
    companion object {
        private val alphabet = ('0' .. '9').toList() + ('A'..'z').toList()
        private val base = 62
    }

    override fun encode(n: Long): String {
        var n = n
        return if (n == 0L) "0"
        else {
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