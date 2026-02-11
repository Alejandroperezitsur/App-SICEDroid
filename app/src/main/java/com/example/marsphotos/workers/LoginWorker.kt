package com.example.marsphotos.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.marsphotos.MarsPhotosApplication

class LoginWorker(ctx: Context, params: WorkerParameters)
    : CoroutineWorker(ctx,params){

        val net = ctx as MarsPhotosApplication

    override suspend fun doWork(): Result {
        //TODO("Not yet implemented")
        val u = net.container.snRepository.acceso("", "")

        return Result.success(workDataOf("u" to u))

    }

}
