package org.ascii.asciiPayCompanion.serverConnection

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.ascii.asciiPayCompanion.LoginMutation
import org.ascii.asciiPayCompanion.Utils
import org.ascii.asciiPayCompanion.accountManagement.AccountSession

class AccountPreparationWorker(
    appContext: Context,
    // expected parameter data:
    // username: string
    // password: string
    workerParams: WorkerParameters,
) :
CoroutineWorker(appContext, workerParams){
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // extract username and password from workerParams
                inputData.getString("username")?.let { username ->
                    inputData.getString("password")?.let { password ->
                        // build an unprivileged client to do the password login
                        val apolloClient = ApolloClient.Builder()
                            .serverUrl(Utils.serverURL)
                            .build()
                        // 1. Login with the username + password
                        apolloClient.mutation(
                            LoginMutation(
                                username = username,
                                password = password,
                            )
                        )
                            .execute()
                            .data?.login?.authorization?.let {
                                val okHttpClient = OkHttpClient.Builder()
                                    .addInterceptor(AccountSession.AuthenticationInterceptor(it))
                                    .build()
                                // build a privileged client to fetch card data
                                val privilegedClient = ApolloClient.Builder()
                                    .serverUrl(Utils.serverURL)
                                    .build()
                                // 2. check if the account has an ISO card registered and fetch it
                                // TODO create card data fetch Requests

                                // 3. If no ISO card is present create one and fetch its data

                                // TODO add return data to result type
                                return@withContext Result.success()
                            }?:let {
                            // TODO check if it is a real Login Failure
                                return@withContext Result.failure(
                                    Data.Builder().putInt("error", ServerError.LoginFailure.ordinal).build()
                                )
                        }
                    }?:let {
                        return@withContext Result.failure()
                    }
                }?:let{
                    // return if no login data is provided by the app
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