package com.barsentinel.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database (spec §13): all 13 entities, version 1. Schema export is
 * off for Phase 1; from the first release onward, migrations are tested and
 * the timeline and vault must survive every upgrade intact.
 */
@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        EnvelopeEntity::class,
        CompilerInputEntity::class,
        PrecautionEntity::class,
        AcceptanceEntity::class,
        CheckInEntity::class,
        EscalationEntity::class,
        LocationFixEntity::class,
        TimelineEventEntity::class,
        EvidenceItemEntity::class,
        OutboxMessageEntity::class,
        IncidentRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BarSentinelDatabase : RoomDatabase() {
    abstract fun envelopeDao(): EnvelopeDao
    abstract fun compilerDao(): CompilerDao
    abstract fun timelineDao(): TimelineDao
    abstract fun contactDao(): ContactDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        const val NAME = "barsentinel.db"
    }
}
