package com.barsentinel.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * Audit entities (spec §10, §13). The timeline is append-only: the data
 * layer exposes no update or delete path for timeline events. Evidence
 * hashes are computed at capture, before any sync.
 */

@Entity(
    tableName = "timeline_events",
    primaryKeys = ["envelopeId", "sequence"],
    indices = [Index(value = ["envelopeId"])]
)
data class TimelineEventEntity(
    val envelopeId: String,
    val sequence: Long,
    val type: String,
    /** UTC epoch millis; the hash chain uses the canonical UTC form. */
    val timestampEpochMillis: Long,
    val canonicalPayload: String,
    val previousHash: String,
    val eventHash: String
)

@Entity(
    tableName = "evidence_items",
    indices = [Index(value = ["envelopeId"])]
)
data class EvidenceItemEntity(
    @androidx.room.PrimaryKey val id: String,
    val envelopeId: String,
    /** PHOTO, NOTE */
    val type: String,
    val fileReference: String,
    val sha256: String,
    val capturedAtEpochMillis: Long,
    val accessLogReference: String?
)

/** Guided incident record. Local-first, included in exports, never auto-submitted. */
@Entity(tableName = "incident_records")
data class IncidentRecordEntity(
    @androidx.room.PrimaryKey val envelopeId: String,
    val category: String,
    val narrative: String,
    val outcome: String?,
    /** Comma-separated evidence item ids. */
    val evidenceIdsCsv: String
)
