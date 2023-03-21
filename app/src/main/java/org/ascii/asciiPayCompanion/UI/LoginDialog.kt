package org.ascii.asciiPayCompanion.UI

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import org.ascii.asciiPayCompanion.R
import org.ascii.asciiPayCompanion.accountManagement.AccountDataManager
import org.ascii.asciiPayCompanion.accountManagement.LoginError

class LoginDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext()).setView(R.layout.password_prompt)
            .setCancelable(true)
            // TODO add action
            .setPositiveButton(getString(R.string.LoginOption)) { _, _ ->
            }
            .setNegativeButton(getString(android.R.string.cancel)) { _, _ -> }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val username =
                dialog.findViewById<TextInputEditText>(R.id.AccountEmail)!!.text.toString()
            val password =
                dialog.findViewById<TextInputEditText>(R.id.AccountPassword)!!.text.toString()
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton =  dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val usernameContainer
                = dialog.findViewById<TextInputLayout>(R.id.AccountEmailContainer)!!
            val passwordContainer
                = dialog.findViewById<TextInputLayout>(R.id.AccountPasswordContainer)!!
            val progressIndicator =
                dialog.findViewById<CircularProgressIndicator>(R.id.progress_circular)!!
            val errorMessage =
                dialog.findViewById<MaterialTextView>(R.id.LoginErrorMessage)!!

            positiveButton.visibility = View.GONE
            negativeButton.visibility = View.GONE
            usernameContainer.visibility = View.GONE
            passwordContainer.visibility = View.GONE
            progressIndicator.visibility = View.VISIBLE

            AccountDataManager.login(username, password, {
                dialog.cancel()
            }, {loginError ->
                requireActivity().runOnUiThread {
                    progressIndicator.visibility = View.GONE
                    errorMessage.text = resources.getString(when(loginError){
                        LoginError.networkUnavailable -> R.string.networkUnavailable
                        LoginError.wrongCredentials -> R.string.WrongCredentials
                        LoginError.unknown -> R.string.unknownLoginError
                    })
                    usernameContainer.visibility = View.VISIBLE
                    passwordContainer.visibility = View.VISIBLE
                    errorMessage.visibility = View.VISIBLE
                    positiveButton.visibility = View.VISIBLE
                    negativeButton.visibility = View.VISIBLE
                }
            })

        }
        return dialog
    }

    companion object {
        const val TAG = "LoginDialog"
    }

}