package com.example.upisplitter.domain

import kotlin.random.Random

object AmountSplitter {
    const val MIN_SPLIT_AMOUNT = 2000L
    const val MAX_CHUNK_AMOUNT = 1999L

    fun split(totalAmount: Long): List<Long> {
        if (totalAmount < MIN_SPLIT_AMOUNT) return emptyList()

        val chunks = mutableListOf<Long>()
        var remaining = totalAmount

        while (remaining > MAX_CHUNK_AMOUNT) {
            val chunkVal = Random.nextLong(1990L, MAX_CHUNK_AMOUNT + 1L)
            chunks.add(chunkVal)
            remaining -= chunkVal
        }

        if (remaining > 0L) {
            chunks.add(remaining)
        }

        return chunks
    }
}
