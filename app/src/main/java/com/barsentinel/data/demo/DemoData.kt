package com.barsentinel.data.demo

/**
 * Synthetic demo content for the Phase 1 Home screen. Every name, place and
 * number here is invented for the showcase; no real client, venue or person
 * appears in any BarSentinel demo asset (spec §11). The DEMO / SIMULATION
 * labels on screen say so, and this object is deleted when real envelopes
 * land in Phase 3.
 */
data class DemoEnvelope(
    val id: String,
    val type: String,
    val area: String,
    val buddyName: String,
    val buddyInitial: String,
    val acceptedVersion: Int,
    val acceptedAt: String,
    val minutesLeft: Int,
    val fractionLeft: Float,
    val checkInCurrent: Int,
    val checkInTotal: Int,
    val intervalMinutes: Int,
    val timelineEvents: Int
)

object DemoData {
    val liveEnvelope = DemoEnvelope(
        id = "demo-envelope-1",
        type = "Client meeting",
        area = "Dhanmondi",
        buddyName = "Rahim Uddin",
        buddyInitial = "R",
        acceptedVersion = 2,
        acceptedAt = "14:02",
        minutesLeft = 24,
        fractionLeft = 0.6f,
        checkInCurrent = 2,
        checkInTotal = 5,
        intervalMinutes = 30,
        timelineEvents = 6
    )

    const val VAULT_ITEM_COUNT = 3
    const val LAST_ENVELOPE_LABEL = "Client meeting · Closed safely"
}
