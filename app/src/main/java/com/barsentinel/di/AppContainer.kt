package com.barsentinel.di

import android.content.Context
import androidx.room.Room
import com.barsentinel.data.local.BarSentinelDatabase

/**
 * Explicit construction root. Phase 1 needs only the database; repositories
 * and use-cases are added here as each phase lands, so construction stays
 * visible in one file instead of scattered across ViewModels.
 */
class AppContainer(context: Context) {

    val database: BarSentinelDatabase = Room.databaseBuilder(
        context.applicationContext,
        BarSentinelDatabase::class.java,
        BarSentinelDatabase.NAME
    ).build()

    val envelopeDao get() = database.envelopeDao()
    val timelineDao get() = database.timelineDao()
    val contactDao get() = database.contactDao()
    val evidenceDao get() = database.evidenceDao()
    val outboxDao get() = database.outboxDao()
}
