package com.chesspuzzles.woodpecker

import android.app.Application
import com.chesspuzzles.woodpecker.data.csv.CsvImporter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WoodpeckerApp : Application() {

    @Inject
    lateinit var csvImporter: CsvImporter

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            csvImporter.importIfNeeded()
        }
    }
}
