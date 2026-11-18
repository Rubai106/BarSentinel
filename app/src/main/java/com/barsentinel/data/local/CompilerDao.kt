package com.barsentinel.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Compiler inputs and precautions per envelope version. A draft save writes
 * the input and the compiled precautions together; invalidation (Phase 7)
 * diffs the stored input of one version against the next.
 */
@Dao
interface CompilerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInput(input: CompilerInputEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrecautions(precautions: List<PrecautionEntity>)

    @Query("SELECT * FROM compiler_inputs WHERE envelopeId = :envelopeId AND envelopeVersion = :version")
    suspend fun getInput(envelopeId: String, version: Int): CompilerInputEntity?

    @Query("SELECT * FROM precautions WHERE envelopeId = :envelopeId AND envelopeVersion = :version")
    fun getPrecautions(envelopeId: String, version: Int): Flow<List<PrecautionEntity>>
}
