package com.barsentinel.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Envelope versions and lifecycle. Versions are never overwritten. */
@Dao
interface EnvelopeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(envelope: EnvelopeEntity)

    @Update
    suspend fun update(envelope: EnvelopeEntity)

    @Query("SELECT * FROM envelopes WHERE id = :id AND version = :version")
    suspend fun getVersion(id: String, version: Int): EnvelopeEntity?

    @Query("SELECT * FROM envelopes WHERE id = :id ORDER BY version DESC")
    fun getAllVersions(id: String): Flow<List<EnvelopeEntity>>

    @Query(
        "SELECT * FROM envelopes e WHERE version = " +
            "(SELECT MAX(version) FROM envelopes WHERE id = e.id) ORDER BY createdAtEpochMillis DESC"
    )
    fun getLatestVersions(): Flow<List<EnvelopeEntity>>
}

/** Append-only timeline access. There is intentionally no update or delete. */
@Dao
interface TimelineDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: TimelineEventEntity)

    @Query("SELECT * FROM timeline_events WHERE envelopeId = :envelopeId ORDER BY sequence ASC")
    fun getByEnvelope(envelopeId: String): Flow<List<TimelineEventEntity>>

    @Query("SELECT * FROM timeline_events WHERE envelopeId = :envelopeId ORDER BY sequence DESC LIMIT 1")
    suspend fun getLatest(envelopeId: String): TimelineEventEntity?
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface EvidenceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: EvidenceItemEntity)

    @Query("SELECT * FROM evidence_items WHERE envelopeId = :envelopeId ORDER BY capturedAtEpochMillis ASC")
    fun getByEnvelope(envelopeId: String): Flow<List<EvidenceItemEntity>>
}

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: OutboxMessageEntity)

    @Query("SELECT * FROM outbox_messages WHERE state = 'QUEUED' ORDER BY nextRetryAtEpochMillis ASC")
    suspend fun getPending(): List<OutboxMessageEntity>

    @Query("UPDATE outbox_messages SET state = :state, channelAttempts = :attempts WHERE id = :id")
    suspend fun updateState(id: String, state: String, attempts: Int)

    @Query("DELETE FROM outbox_messages WHERE id = :id")
    suspend fun delete(id: String)
}
