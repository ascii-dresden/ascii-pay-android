package org.ascii.asciiPayCompanion.UI

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import org.ascii.asciiPayCompanion.R
import org.ascii.asciiPayCompanion.accountManagement.AccountDataManager
import org.ascii.asciiPayCompanion.api.AccountDto
import org.ascii.asciiPayCompanion.api.Api
import org.ascii.asciiPayCompanion.api.AuthResponseDto
import org.ascii.asciiPayCompanion.api.ResultHandler

class LoginDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).setView(R.layout.password_prompt)
            .setCancelable(true)
            // TODO add action
            .setPositiveButton(getString(R.string.LoginOption)) { _, _ ->
                val dialog = dialog ?: run { throw UnknownError() }
                val username =
                    dialog.findViewById<TextInputEditText>(R.id.AccountEmail).text.toString()
                val password =
                    dialog.findViewById<TextInputEditText>(R.id.AccountPassword).text.toString()

                Api.login(username, password, object : ResultHandler<AuthResponseDto> {
                    override fun onSuccess(value: AuthResponseDto) {
                        Log.e("LOGIN", "Success: ${value.token}")

                        Api(value.token).getSelf(object : ResultHandler<AccountDto> {
                            override fun onSuccess(value: AccountDto) {
                                Log.e("ACCOUNT", "Success: $value")
                            }

                            override fun onError(status: Int, error: String) {
                                Log.e("ACCOUNT", "Error $status")
                            }
                        })
                    }

                    override fun onError(status: Int, error: String) {
                        Log.e("LOGIN", "Error $status")
                    }
                })
            }.setNegativeButton(getString(android.R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .create()
    }

    companion object {
        const val TAG = "LoginDialog"
    }

}