package com.barsentinel

import android.app.Application
import com.barsentinel.di.AppContainer

/**
 * Application entry point. Holds the [AppContainer], the single place where
 * the database and repositories are built (Phase 1 uses explicit construction
 * instead of a DI framework; Hilt arrives with the backend in Phase 3 —
 * see docs/architecture.md).
 */
class BarSentinelApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
