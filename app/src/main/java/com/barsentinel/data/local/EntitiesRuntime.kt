package com.barsentinel.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * Runtime entities (spec §8, §9, §13). Escalation rows record failures too:
 * one row per step attempt, so the timing table (Gate G8) can include
 * failures instead of hiding them.
 */

@Entity(
    tableName = "check_ins",
    indices = [Index(value = ["envelopeId"])]
)
data class CheckInEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val envelopeId: String,
    val timestampEpochMillis: Long,
    /** APP, NOTIFICATION */
    val source: String,
    /** Delivery state machine, shared with all messages. */
    val deliveryState: String
)

@Entity(
    tableName = "escalations",
    indices = [Index(value = ["envelopeId"])]
)
data class EscalationEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val envelopeId: String,
    val step: Int,
    val triggeredAtEpochMillis: Long,
    /** PUSH, SMS, QUEUE */
    val channel: String,
    val deliveryState: String,
    val acknowledgedBy: String?,
    val acknowledgedAtEpochMillis: Long?,
    val stoodDownBy: String?,
    val stoodDownAtEpochMillis: Long?
)

/**
 * Escrowed location (spec §9). Only the latest fix is retained during a
 * healthy envelope — one row per envelope. Released only at escalation
 * Step 2 or SOS; deleted on safe closure, with the deletion logged.
 */
@Entity(tableName = "location_fixes")
data class LocationFixEntity(
    @androidx.room.PrimaryKey val envelopeId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampEpochMillis: Long,
    val releasedTo: String?,
    val releasedAtEpochMillis: Long?,
    val deletedAtEpochMillis: Long?
)

/** Outbox drained by WorkManager (Phase 4). State is user-visible: queued is never shown as sent. */
@Entity(
    tableName = "outbox_messages",
    indices = [Index(value = ["envelopeId"])]
)
data class OutboxMessageEntity(
    @androidx.room.PrimaryKey val id: String,
    val envelopeId: String,
    val kind: String,
    val channelAttempts: Int,
    /** QUEUED, SENT, DELIVERED, FAILED */
    val state: String,
    val nextRetryAtEpochMillis: Long?
)
