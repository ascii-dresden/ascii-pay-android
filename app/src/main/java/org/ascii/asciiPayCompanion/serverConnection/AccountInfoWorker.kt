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
import org.ascii.asciiPayCompanion.AccountInfoQuery
import org.ascii.asciiPayCompanion.accountManagement.AccountSession

class AccountInfoWorker(
    appContext: Context,
    // expected parameter data:
    // authorization : String
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO){
        try {
            inputData.getString("authorization")?.let { auth ->
                val data: AccountInfoQuery.Data = AccountSession.privilegedRequest(auth){ client ->
                    client.query(AccountInfoQuery())
                }
                return@withContext Result.success(
                    Data.Builder()
                        .putString("name", data.getAccount.name)
                        .putString("id", data.getAccount.id.toString())
                        .build())
            }?:let{
                // return if no card data is provided by the app
                // this should result in an Exception by the workers client
                return@withContext Result.failure()
            }
        }
        catch (e: ApolloException) {
            return@withContext Result.failure(
                Data.Builder().putInt("error", ServerError.NetworkUnavailable.ordinal).build()
            )
        }
    }
}