package com.example.songguessinggame

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker to update markers daily.
 *
 * @param context the context of the class.
 * @param workerParams the workers parameters.
 * @constructor Creates a worker.
 */
class UpdateMarkers(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {
    private lateinit var context: Context

    override fun doWork(): Result {
        return try {
            DatabaseHandler(context).populateMarkersTable()
            Result.success()
        } catch (e :Exception) {
            Result.failure()
        }
    }
}