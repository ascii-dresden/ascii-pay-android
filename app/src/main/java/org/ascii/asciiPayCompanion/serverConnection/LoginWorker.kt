package org.ascii.asciiPayCompanion.serverConnection

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ascii.asciiPayCompanion.accountManagement.AccountSession

class LoginWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            AccountSession.login()
            // TODO Error handling
            return@withContext Result.success()
        } catch (e: ApolloException) {
            return@withContext Result.failure(
                Data.Builder().putInt("error", ServerError.networkNotAvailable.ordinal).build()
            )
        }
    }
}