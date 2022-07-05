package org.ascii.asciiPayCompanion.accountManagement

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.apollographql.apollo3.ApolloClient
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.ascii.asciiPayCompanion.LoginMutation
import org.ascii.asciiPayCompanion.Utils

class IncompleteAccount(
    private val username: String,
    private val password: String,
    private val lifecycle: Lifecycle,
) {
    val apolloClient = ApolloClient.Builder()
        .serverUrl(Utils.serverURL)
        .build()

    fun prepareAccount(
        completionCallback: (CardData) -> Unit,
        completionFailCallback: (AccountCompletionError) -> Unit
    ) {
        lifecycle.coroutineScope.launch {
            // 1. Login with the username + password
            val privilegedClient: ApolloClient
            apolloClient.mutation(LoginMutation(username = username, password = password))
                .execute()
                .data?.login?.authorization?.let {
                    val okHttpClient = OkHttpClient.Builder()
                        .addInterceptor(Credentials.AuthenticationInterceptor(it))
                        .build()
                    privilegedClient = ApolloClient.Builder()
                        .serverUrl(Utils.serverURL)
                        .build()
                } ?:let {
                    completionFailCallback(AccountCompletionError.InvalidCredentials)
                    return@let
                }

                // 2. check if the account has an ISO card registered and fetch it

                // 3. If no ISO card is present create one and fetch its data
            }
        }
    }

    enum class AccountCompletionError {
        InvalidCredentials,
        Unknown,
    }
}