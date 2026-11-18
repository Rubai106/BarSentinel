package com.barsentinel.domain.chain

import java.security.MessageDigest

/**
 * Hash-chained timeline (spec §10). Every envelope event is appended as an
 * immutable record containing the previous event's hash:
 *
 *   event_hash = SHA-256(previous_hash | envelope_id | event_type | timestamp | canonical_payload)
 *
 * Timestamps use the canonical UTC epoch-millis form. The chain proves the
 * record has not changed since it was written; it does not prove the events
 * themselves were true, and no screen may claim otherwise.
 *
 * Pure JVM code — no Android dependency — so the builder and verifier run as
 * plain unit tests and in the documented independent verification script.
 */
object HashChain {

    /** Previous-hash value for the first event of an envelope. */
    const val GENESIS_PREVIOUS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"

    data class ChainEvent(
        val envelopeId: String,
        val sequence: Long,
        val eventType: String,
        /** UTC epoch millis; display converts to local time. */
        val timestampEpochMillis: Long,
        /** Canonical payload: stable key order, no whitespace variation. */
        val canonicalPayload: String,
        val previousHash: String,
        val eventHash: String
    )

    sealed interface Verification {
        data object Valid : Verification
        data object Empty : Verification
        data class Broken(val sequence: Long, val reason: String) : Verification
    }

    fun hashEvent(
        previousHash: String,
        envelopeId: String,
        eventType: String,
        timestampEpochMillis: Long,
        canonicalPayload: String
    ): String {
        val material = listOf(
            previousHash,
            envelopeId,
            eventType,
            timestampEpochMillis.toString(),
            canonicalPayload
        ).joinToString("|")
        return sha256Hex(material)
    }

    /** Builds the next event in a chain, linking it to [previousEvent]. */
    fun append(
        previousEvent: ChainEvent?,
        envelopeId: String,
        eventType: String,
        timestampEpochMillis: Long,
        canonicalPayload: String
    ): ChainEvent {
        val previousHash = previousEvent?.eventHash ?: GENESIS_PREVIOUS_HASH
        val sequence = (previousEvent?.sequence ?: 0L) + 1L
        return ChainEvent(
            envelopeId = envelopeId,
            sequence = sequence,
            eventType = eventType,
            timestampEpochMillis = timestampEpochMillis,
            canonicalPayload = canonicalPayload,
            previousHash = previousHash,
            eventHash = hashEvent(
                previousHash,
                envelopeId,
                eventType,
                timestampEpochMillis,
                canonicalPayload
            )
        )
    }

    /**
     * Verifies a chain. Events must be in sequence order. The verifier names
     * the first broken link — it does not merely say "invalid" (spec §10).
     */
    fun verify(events: List<ChainEvent>): Verification {
        if (events.isEmpty()) return Verification.Empty
        var expectedPrevious = GENESIS_PREVIOUS_HASH
        var expectedSequence = 1L
        for (event in events) {
            if (event.sequence != expectedSequence) {
                return Verification.Broken(
                    event.sequence,
                    "Sequence gap or reorder: expected sequence $expectedSequence, found ${event.sequence}."
                )
            }
            if (event.previousHash != expectedPrevious) {
                return Verification.Broken(
                    event.sequence,
                    "Previous-hash link broken: event does not link to the preceding event."
                )
            }
            val recomputed = hashEvent(
                event.previousHash,
                event.envelopeId,
                event.eventType,
                event.timestampEpochMillis,
                event.canonicalPayload
            )
            if (recomputed != event.eventHash) {
                return Verification.Broken(
                    event.sequence,
                    "Event hash mismatch: the event contents changed after writing."
                )
            }
            expectedPrevious = event.eventHash
            expectedSequence++
        }
        return Verification.Valid
    }

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
