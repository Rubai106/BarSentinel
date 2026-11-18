package com.barsentinel.domain.chain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hash-chain tests (spec §10, Gate G5 at unit level). The chain must verify
 * clean bundles, fail tampered ones at the altered event, and fail deletions
 * and reorders. The full gate also needs the live tamper demo (Phase 6).
 */
class HashChainTest {

    private fun buildChain(n: Int): List<HashChain.ChainEvent> {
        val events = mutableListOf<HashChain.ChainEvent>()
        var previous: HashChain.ChainEvent? = null
        repeat(n) { i ->
            val next = HashChain.append(
                previous,
                envelopeId = "env-1",
                eventType = "EVENT_$i",
                timestampEpochMillis = 1_700_000_000_000L + i * 1_000L,
                canonicalPayload = """{"i":$i}"""
            )
            events += next
            previous = next
        }
        return events
    }

    @Test
    fun sha256MatchesKnownVector() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            HashChain.sha256Hex("abc")
        )
    }

    @Test
    fun hashingIsDeterministicAndSensitiveToPayload() {
        val a = HashChain.hashEvent(HashChain.GENESIS_PREVIOUS_HASH, "env-1", "CREATED", 1000L, "{}")
        val b = HashChain.hashEvent(HashChain.GENESIS_PREVIOUS_HASH, "env-1", "CREATED", 1000L, "{}")
        val c = HashChain.hashEvent(HashChain.GENESIS_PREVIOUS_HASH, "env-1", "CREATED", 1000L, """{"x":1}""")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun cleanChainVerifies() {
        assertEquals(HashChain.Verification.Valid, HashChain.verify(buildChain(5)))
    }

    @Test
    fun sequencesStartAtOneAndLinkToGenesis() {
        val events = buildChain(3)
        assertEquals(1L, events.first().sequence)
        assertEquals(HashChain.GENESIS_PREVIOUS_HASH, events.first().previousHash)
        assertEquals(events[0].eventHash, events[1].previousHash)
    }

    @Test
    fun tamperedPayloadFailsAtTheAlteredEvent() {
        val events = buildChain(5).toMutableList()
        val tampered = events[2].copy(canonicalPayload = """{"i":999}""")
        events[2] = tampered
        val result = HashChain.verify(events)
        assertTrue(result is HashChain.Verification.Broken)
        result as HashChain.Verification.Broken
        assertEquals(3L, result.sequence)
        assertTrue(result.reason.contains("hash mismatch", ignoreCase = true))
    }

    @Test
    fun deletedEventFailsAtTheGap() {
        val events = buildChain(5).toMutableList()
        events.removeAt(2)
        val result = HashChain.verify(events)
        assertTrue(result is HashChain.Verification.Broken)
        result as HashChain.Verification.Broken
        assertEquals(4L, result.sequence)
    }

    @Test
    fun reorderedEventsFail() {
        val events = buildChain(4)
        val reordered = listOf(events[0], events[2], events[1], events[3])
        assertTrue(HashChain.verify(reordered) is HashChain.Verification.Broken)
    }

    @Test
    fun emptyChainReportsEmpty() {
        assertEquals(HashChain.Verification.Empty, HashChain.verify(emptyList()))
    }
}
