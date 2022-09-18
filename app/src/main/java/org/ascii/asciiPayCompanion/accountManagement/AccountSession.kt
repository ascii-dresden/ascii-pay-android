package org.ascii.asciiPayCompanion.accountManagement

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.work.ListenableWorker.*
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.network.okHttpClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.ascii.asciiPayCompanion.LoginMutation
import org.ascii.asciiPayCompanion.Utils
import org.ascii.asciiPayCompanion.Utils.Companion.serverURL
import org.ascii.asciiPayCompanion.serverConnection.ServerError

class AccountSession(
    private val authenticationCard: CardData,
    private val loginFailCallback: () -> Unit,
) {

    private val unprivilegedClient by lazy {
        ApolloClient.Builder()
            .serverUrl(serverURL)
            .build()
    }

    private var privilegedClient: ApolloClient? = null

    companion object {
        // TODO replace session with auth token
        suspend fun cardLogin(session: AccountSession) : ServerError {
            // TODO replace with card data login code
            ApolloClient.Builder()
                .serverUrl(serverURL)
                .build()
                .mutation(LoginMutation(username = username, password = password))
                .execute()
                .data?.login?.authorization?.let {
                    return ServerError.NoError
                }?:let{
                    return ServerError.LoginFailure
            }
        }

        suspend fun <D : Operation.Data> privilegedRequest(
            authorization: String,
            operation: (ApolloClient) -> (ApolloCall<D>),
        ) : D
        {
            // TODO think of a way to get provide card data to the scope which is needed for login
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthenticationInterceptor(authorization))
                .build()
            val privilegedClient = ApolloClient.Builder()
                .serverUrl(serverURL)
                .okHttpClient(okHttpClient)
                .build()

            privilegedClient?.let(operation)?.execute()
                ?.data?.let {
                    return(it)
                } ?: let {
                // retry with a new login
                cardLogin()
                privilegedClient?.let(operation)?.execute()
                    ?.data?.let {
                        return it
                    }
            }

        }

        private fun networkError(){
            Log.e(Utils.TAG, "Failed to do graphql operation: Server not available")
        }
    }

    class AuthenticationInterceptor(private val authorization: String): Interceptor{
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .addHeader("Authorization", authorization)
                .build()
            return chain.proceed(request)
        }

    }
}
