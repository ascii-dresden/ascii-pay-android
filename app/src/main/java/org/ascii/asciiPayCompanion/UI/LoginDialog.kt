package org.ascii.asciiPayCompanion.UI

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import org.ascii.asciiPayCompanion.R
import org.ascii.asciiPayCompanion.accountManagement.AccountDto
import org.ascii.asciiPayCompanion.accountManagement.Api
import org.ascii.asciiPayCompanion.accountManagement.AuthResponseDto
import org.ascii.asciiPayCompanion.accountManagement.ResultHandler

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

                        val api = Api(value.token);
                        api.getSelf(object : ResultHandler<AccountDto> {
                            override fun onSuccess(value: AccountDto) {
                                Log.e("ACCOUNT", "Success: $value")

                                api.createNfcCard(
                                    value.id,
                                    "Ascii Pay Card",
                                    byteArrayOf(10, 15, 20, 25),
                                    byteArrayOf(10, 15, 20, 25),
                                    object : ResultHandler<Unit> {
                                        override fun onSuccess(value: Unit) {
                                            Log.e("NFC", "Success")
                                        }

                                        override fun onError(status: Int, error: String) {
                                            Log.e("NFC", "Error $status: $error")
                                        }
                                    })
                            }

                            override fun onError(status: Int, error: String) {
                                Log.e("ACCOUNT", "Error $status: $error")
                            }
                        })
                    }

                    override fun onError(status: Int, error: String) {
                        Log.e("LOGIN", "Error $status: $error")
                    }
                })
            }.setNegativeButton(getString(android.R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .create()
    }

    companion object {
        const val TAG = "LoginDialog"
    }

}