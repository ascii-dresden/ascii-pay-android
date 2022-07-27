package org.ascii.asciiPayCompanion.accountManagement

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.ascii.asciiPayCompanion.Utils
import org.ascii.asciiPayCompanion.Utils.Companion.serverURL

class AccountSession(
    private val authenticationCard: CardData,
    private val lifecycle: Lifecycle,
    private val loginFailCallback: () -> Unit,
) {

    private val unprivilegedClient by lazy {
        ApolloClient.Builder()
            .serverUrl(serverURL)
            .build()
    }

    private var privilegedClient: ApolloClient? = null

    fun <D : Operation.Data> privilegedRequest(
        operation: (ApolloClient) -> (ApolloCall<D>),
        callback: (D) -> Unit
    ) {
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
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

    // change this to use card datas
    suspend fun login() {
        // TODO replace with card data login code
       // unprivilegedClient.mutation(LoginMutation(username = username, password = password))
       //     .execute()
       //     .data?.login?.authorization?.let {
       //         val okHttpClient = OkHttpClient.Builder()
       //             .addInterceptor(AuthenticationInterceptor(it))
       //             .build()
       //         privilegedClient = ApolloClient.Builder()
       //             .serverUrl(serverURL)
       //             .build()
       //     } ?: loginFailCallback()
    }

    private fun networkError() {
        Log.e(Utils.TAG, "Failed to do graphql operation: Server not available")
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
