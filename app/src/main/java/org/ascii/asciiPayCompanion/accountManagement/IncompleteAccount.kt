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
) {

    fun prepareAccount(
        completionCallback: (CardData) -> Unit,
        completionFailCallback: (AccountCompletionError) -> Unit
    ) {


                } ?:let {
                    completionFailCallback(AccountCompletionError.InvalidCredentials)
                    return@let
                }


        }

    enum class AccountCompletionError {
        InvalidCredentials,
        Unknown,
    }
}