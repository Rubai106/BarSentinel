package com.barsentinel.data.local

import androidx.room.Entity

/**
 * Compiler entities (spec §7, §13). Compiler inputs are stored per envelope
 * version so invalidation can diff original vs changed inputs. Precautions
 * are data, not just UI text: the explanation survives export.
 */

@Entity(tableName = "compiler_inputs", primaryKeys = ["envelopeId", "envelopeVersion"])
data class CompilerInputEntity(
    val envelopeId: String,
    val envelopeVersion: Int,
    val engagementType: String,
    val venueFamiliarity: String,
    val finishesAfterDark: Boolean,
    val durationMinutes: Int,
    val company: String,
    val counterparty: String?,
    val travel: String,
    val connectivity: String
)

@Entity(tableName = "precautions", primaryKeys = ["envelopeId", "envelopeVersion", "ruleId"])
data class PrecautionEntity(
    val envelopeId: String,
    val envelopeVersion: Int,
    val ruleId: String,
    /** REQUIRED, RECOMMENDED, CONSIDER */
    val level: String,
    val action: String,
    val triggerText: String,
    /** ACTIVE, DISMISSED, INVALIDATED */
    val state: String,
    /** Dismissal or invalidation reason; null while active. */
    val stateReason: String?
)

/** A buddy's decision on one exact envelope version. Acceptance of a superseded version is itself an event. */
@Entity(tableName = "acceptances", primaryKeys = ["envelopeId", "envelopeVersion", "buddyId"])
data class AcceptanceEntity(
    val envelopeId: String,
    val envelopeVersion: Int,
    val buddyId: String,
    /** ACCEPTED, DECLINED */
    val decision: String,
    val timestampEpochMillis: Long
)
