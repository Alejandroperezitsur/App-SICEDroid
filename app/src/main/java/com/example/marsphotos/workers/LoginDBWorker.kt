package com.example.marsphotos.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class LoginDBWorker (ctx: Context, params: WorkerParameters) :
    CoroutineWorker (ctx, params) {
    override suspend fun doWork(): Result {

        val resourceUri = inputData.getString("u")
        //TODO("Not yet implemented")
        //Hay que ir a Room
        //dao.insert(u)
        return  Result.success()
    }

}
