package data_classes

data class HashResult(
    val sha1sum: ByteArray,
    val sha256sum: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HashResult

        if (!sha1sum.contentEquals(other.sha1sum)) return false
        if (!sha256sum.contentEquals(other.sha256sum)) return false

        return true
    }

    override fun hashCode(): Int {
        return sha1sum.contentHashCode() * 3 + sha256sum.contentHashCode() * 13
    }
}
