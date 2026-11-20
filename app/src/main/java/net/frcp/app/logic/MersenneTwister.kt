package net.frcp.app.logic

import java.security.SecureRandom

class MersenneTwister private constructor() {
    private val mt = IntArray(N)
    private var index = N

    constructor(key: IntArray = secureSeed()) : this() {
        seedArray(key)
    }

    constructor(seed: Int) : this() {
        seed(seed)
    }

    fun seed(s: Int): MersenneTwister {
        mt[0] = s
        for (i in 1 until N) {
            val prev = mt[i - 1] xor (mt[i - 1] ushr 30)
            mt[i] = 1812433253 * prev + i
        }
        index = N
        return this
    }

    fun seedArray(key: IntArray): MersenneTwister {
        require(key.isNotEmpty()) { "Seed array must not be empty" }
        seed(19650218)
        var i = 1
        var j = 0
        for (k in maxOf(N, key.size) downTo 1) {
            val prev = mt[i - 1] xor (mt[i - 1] ushr 30)
            mt[i] = (mt[i] xor (prev * 1664525)) + key[j] + j
            i++
            j++
            if (i >= N) {
                mt[0] = mt[N - 1]
                i = 1
            }
            if (j >= key.size) j = 0
        }
        for (k in N - 1 downTo 1) {
            val prev = mt[i - 1] xor (mt[i - 1] ushr 30)
            mt[i] = (mt[i] xor (prev * 1566083941)) - i
            i++
            if (i >= N) {
                mt[0] = mt[N - 1]
                i = 1
            }
        }
        mt[0] = UPPER_MASK
        index = N
        return this
    }

    private fun twist() {
        for (k in 0 until N) {
            val y = (mt[k] and UPPER_MASK) or (mt[(k + 1) % N] and LOWER_MASK)
            mt[k] = mt[(k + M) % N] xor (y ushr 1) xor (if ((y and 1) != 0) MATRIX_A else 0)
        }
        index = 0
    }

    fun nextUint32(): Long {
        if (index >= N) twist()
        var y = mt[index++]
        y = y xor (y ushr 11)
        y = y xor ((y shl 7) and TEMPER_B)
        y = y xor ((y shl 15) and TEMPER_C)
        y = y xor (y ushr 18)
        return y.toLong() and 0xffffffffL
    }

    fun int(min: Int, max: Int): Int {
        require(max >= min) { "min and max must be integers with min <= max" }
        val range = max.toLong() - min + 1
        val limit = RANGE - RANGE % range
        var r: Long
        do r = nextUint32() while (r >= limit)
        return (min + r % range).toInt()
    }

    companion object {
        private const val N = 624
        private const val M = 397
        private const val RANGE = 4294967296L
        private const val UPPER_MASK = 0x80000000.toInt()
        private const val LOWER_MASK = 0x7fffffff
        private const val MATRIX_A = 0x9908b0df.toInt()
        private const val TEMPER_B = 0x9d2c5680.toInt()
        private const val TEMPER_C = 0xefc60000.toInt()

        fun secureSeed(words: Int = N): IntArray {
            val random = SecureRandom()
            return IntArray(words) { random.nextInt() }
        }
    }
}
