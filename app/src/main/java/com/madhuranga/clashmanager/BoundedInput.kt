package com.madhuranga.clashmanager

import java.io.ByteArrayOutputStream
import java.io.InputStream

/** Reads at most maxBytes, rejecting oversized files before allocating unbounded memory. */
fun readUtf8Limited(input: InputStream, maxBytes: Int): String {
    require(maxBytes > 0) { "Maximum size must be positive" }
    val output = ByteArrayOutputStream(minOf(maxBytes, 8192))
    val buffer = ByteArray(8192)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        if (count == 0) continue
        require(total + count <= maxBytes) { "File is too large. Maximum supported size is ${maxBytes / (1024 * 1024)} MB" }
        output.write(buffer, 0, count)
        total += count
    }
    return output.toString(Charsets.UTF_8.name())
}
