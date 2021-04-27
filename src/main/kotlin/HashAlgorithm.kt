import data_classes.HashResult
import java.nio.ByteBuffer
import java.nio.IntBuffer


class HashAlgorithm {
    companion object {
        private fun sha1sum(input: ByteArray): ByteArray {
            return Sha1Algorithm.hash(input)
        }

        private fun sha256sum(input: ByteArray): ByteArray {
            return Sha256Algorithm.hash(input)
        }

        fun hash(input: ByteArray): HashResult {
            return HashResult(
                sha1sum(input),
                sha256sum(input),
            )
        }
    }
}

class Sha1Algorithm {
    companion object {
        private infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)
        private fun rol(num: Int, cnt: Int): Int {
            return num shl cnt or (num ushr 32 - cnt)
        }

        private fun fill(value: Int, arr: ByteArray, off: Int) {
            arr[off + 0] = (value shr 24 and 0xff).toByte()
            arr[off + 1] = (value shr 16 and 0xff).toByte()
            arr[off + 2] = (value shr 8 and 0xff).toByte()
            arr[off + 3] = (value shr 0 and 0xff).toByte()
        }

        fun hash(input: ByteArray): ByteArray {
            val ml = input.size

            val blocks = IntArray(((input.size + 8 shr 6) + 1) * 16)
            input.forEachIndexed { i, b ->
                blocks[i shr 2] = blocks[i shr 2] or (b shl 24 - i % 4 * 8)
            }

            blocks[ml shr 2] = blocks[ml shr 2] or (0x80 shl 24 - ml % 4 * 8)
            blocks[blocks.size - 1] = ml * 8

            var h0 = 1732584193
            var h1 = -271733879
            var h2 = -1732584194
            var h3 = 271733878
            var h4 = -1009589776

            val w = IntArray(80)
            for (i: Int in blocks.indices step 16) {
                val a = h0
                val b = h1
                val c = h2
                val d = h3
                val e = h4
                for (j in 0..79) {
                    w[j] = if (j < 16) blocks[i + j] else rol(
                        w[j - 3] xor w[j - 8] xor w[j - 14] xor w[j - 16], 1
                    )
                    val t: Int = rol(h0, 5) + h4 + w[j] +
                            if (j < 20) 1518500249 + (h1 and h2 or (h1.inv() and h3)) else if (j < 40) 1859775393 + (h1 xor h2 xor h3) else if (j < 60) -1894007588 + (h1 and h2 or (h1 and h3) or (h2 and h3)) else -899497514 + (h1 xor h2 xor h3)
                    h4 = h3
                    h3 = h2
                    h2 = rol(h1, 30)
                    h1 = h0
                    h0 = t
                }
                h0 += a
                h1 += b
                h2 += c
                h3 += d
                h4 += e
            }
            val digest = ByteArray(20)
            fill(h0, digest, 0)
            fill(h1, digest, 4)
            fill(h2, digest, 8)
            fill(h3, digest, 12)
            fill(h4, digest, 16)

            return digest
        }
    }
}

class Sha256Algorithm {
    companion object {
        private val K = intArrayOf(
            0x428a2f98, 0x71374491, -0x4a3f0431, -0x164a245b, 0x3956c25b, 0x59f111f1, -0x6dc07d5c, -0x54e3a12b,
            -0x27f85568, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, -0x7f214e02, -0x6423f959, -0x3e640e8c,
            -0x1b64963f, -0x1041b87a, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            -0x67c1aeae, -0x57ce3993, -0x4ffcd838, -0x40a68039, -0x391ff40d, -0x2a586eb9, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, -0x7e3d36d2, -0x6d8dd37b,
            -0x5d40175f, -0x57e599b5, -0x3db47490, -0x3893ae5d, -0x2e6d17e7, -0x2966f9dc, -0xbf1ca7b, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, -0x7b3787ec, -0x7338fdf8, -0x6f410006, -0x5baf9315, -0x41065c09, -0x398e870e
        )

        private val H0 = intArrayOf(
            0x6a09e667, -0x4498517b, 0x3c6ef372, -0x5ab00ac6, 0x510e527f, -0x64fa9774, 0x1f83d9ab, 0x5be0cd19
        )

        private const val BLOCK_BITS = 512
        private const val BLOCK_BYTES = BLOCK_BITS / 8

        private val W = IntArray(64)
        private val H = IntArray(8)
        private val TEMP = IntArray(8)

        fun hash(input: ByteArray): ByteArray {
            System.arraycopy(H0, 0, H, 0, H0.size)

            val words = pad(input)

            val n = words.size / 16
            for (i: Int in 0 until n) {
                System.arraycopy(words, i * 16, W, 0, 16)
                for (t in 16 until W.size) {
                    W[t] = smallSig1(W[t - 2]) + W[t - 7] + smallSig0(W[t - 15]) + W[t - 16]
                }

                System.arraycopy(H, 0, TEMP, 0, H.size)

                for (t in W.indices) {
                    val t1 = TEMP[7] + bigSig1(TEMP[4]) + ch(TEMP[4], TEMP[5], TEMP[6]) + K[t] + W[t]
                    val t2 = bigSig0(TEMP[0]) + maj(TEMP[0], TEMP[1], TEMP[2])
                    System.arraycopy(TEMP, 0, TEMP, 1, TEMP.size - 1)
                    TEMP[4] += t1
                    TEMP[0] = t1 + t2
                }

                for (t in H.indices) {
                    H[t] += TEMP[t]
                }
            }

            return toByteArray(H)!!
        }

        private fun pad(input: ByteArray): IntArray {
            val finalBlockLength: Int = input.size % BLOCK_BYTES
            val blockCount: Int = input.size / BLOCK_BYTES + if (finalBlockLength + 1 + 8 > BLOCK_BYTES) 2 else 1

            val result = IntBuffer.allocate(blockCount * (BLOCK_BYTES / Integer.BYTES))

            val n = input.size / Integer.BYTES
            val buf: ByteBuffer = ByteBuffer.wrap(input)

            for (i: Int in 0 until n) {
                result.put(buf.int)
            }

            val remainder: ByteBuffer = ByteBuffer.allocate(4)
            remainder.put(buf).put(128.toByte()).rewind()
            result.put(remainder.int)
            result.position(result.capacity() - 2)

            val msgLength = input.size * 8L
            result.put((msgLength ushr 32).toInt())
            result.put(msgLength.toInt())

            return result.array()
        }

        private fun toByteArray(ints: IntArray): ByteArray? {
            val buf = ByteBuffer.allocate(ints.size * Integer.BYTES)
            for (i in ints) {
                buf.putInt(i)
            }
            return buf.array()
        }

        private fun ch(x: Int, y: Int, z: Int): Int {
            return x and y or (x.inv() and z)
        }

        private fun maj(x: Int, y: Int, z: Int): Int {
            return x and y or (x and z) or (y and z)
        }

        private fun bigSig0(x: Int): Int {
            return (Integer.rotateRight(x, 2)
                    xor Integer.rotateRight(x, 13)
                    xor Integer.rotateRight(x, 22))
        }

        private fun bigSig1(x: Int): Int {
            return (Integer.rotateRight(x, 6)
                    xor Integer.rotateRight(x, 11)
                    xor Integer.rotateRight(x, 25))
        }

        private fun smallSig0(x: Int): Int {
            return (Integer.rotateRight(x, 7)
                    xor Integer.rotateRight(x, 18)
                    xor (x ushr 3))
        }

        private fun smallSig1(x: Int): Int {
            return (Integer.rotateRight(x, 17)
                    xor Integer.rotateRight(x, 19)
                    xor (x ushr 10))
        }
    }
}
