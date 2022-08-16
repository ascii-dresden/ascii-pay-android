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
import okhttp3.OkHttpClient
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

    companion object {
        suspend fun login() {
            // TODO replace with card data login code
            //unprivilegedClient.mutation(LoginMutation(username = username, password = password))
            //    .execute()
            //    .data?.login?.authorization?.let {
            //        val okHttpClient = OkHttpClient.Builder()
            //            .addInterceptor(AuthenticationInterceptor(it))
            //            .build()
            //        privilegedClient = ApolloClient.Builder()
            //            .serverUrl(serverURL)
            //            .build()
            //    } ?: loginFailCallback()
        }

        private fun networkError() {
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
