package org.ascii.asciiPayCompanion.serverConnection

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivilegedWorker(
    appContext: Context,
    // expected parameter data:
    // a privileged ApolloClient
    // operation: (ApolloClient) -> (ApolloCall<D>)
    // loginFailCallback: () -> Unit
    // callback: (D) -> Unit
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO){
        try {
            return@withContext Result.success()
        }
        catch (e: ApolloException) {
            return@withContext Result.failure(
                Data.Builder().putInt("error", ServerError.networkNotAvailable.ordinal).build()
            )
        }
    }

    suspend fun <D : Operation.Data> privilegedRequest(
        operation: (ApolloClient) -> (ApolloCall<D>),
        callback: (D) -> Unit
    ) {
            privilegedClient?.let(operation)?.execute()
                ?.data?.let {
                    callback(it)
                } ?: let {
                // retry with a new login
                login()
                privilegedClient?.let(operation)?.execute()
                    ?.data?.let {
                        callback(it)
                    }
            }

        }
    }
}