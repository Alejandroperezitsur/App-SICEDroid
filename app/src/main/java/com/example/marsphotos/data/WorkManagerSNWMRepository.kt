package com.example.marsphotos.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.marsphotos.workers.LoginDBWorker
import com.example.marsphotos.workers.LoginWorker
import kotlinx.coroutines.flow.Flow

class WorkManagerSNWMRepository(ctx: Context): SNWMRepository {

    private val workManager = WorkManager.getInstance(ctx)
    override val outputWorkInfo: Flow<WorkInfo>
        get() = TODO("Not yet implemented")

    override fun login(m: String, p: String) {
        // Add WorkRequest to Cleanup temporary images
        var continuation = workManager
            .beginUniqueWork(
                "SICENET_MANIPULATION_WORK_NAME",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<LoginWorker>()
                    .addTag("EsteQuieroMonitorear").build()
            )

        // Add WorkRequest to blur the image
        val wrdbWorker = OneTimeWorkRequestBuilder<LoginDBWorker>()

        continuation = continuation.then(wrdbWorker.build())

        // Actually start the work
        continuation.enqueue()

    }

    override fun profile() {
        //TODO("Not yet implemented")
    }

    override fun cargaAcademica() {
        //TODO("Not yet implemented")
    }
}