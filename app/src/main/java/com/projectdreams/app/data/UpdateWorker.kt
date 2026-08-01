package com.projectdreams.app.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projectdreams.app.App

class UpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = App.from(applicationContext)
        return try {
            app.updateChecker.checkAndAct()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
