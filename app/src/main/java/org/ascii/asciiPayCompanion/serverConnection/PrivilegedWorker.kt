package org.ascii.asciiPayCompanion.serverConnection

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException

class PrivilegdedWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {

    }

    suspend fun <D : Operation.Data> privilegedRequest(
        operation: (ApolloClient) -> (ApolloCall<D>),
        callback: (D) -> Unit
    ) {
        try {
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
        } catch (e: ApolloException) {
            networkError()
        }
    }
}