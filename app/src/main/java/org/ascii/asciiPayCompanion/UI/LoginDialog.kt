package org.ascii.asciiPayCompanion.UI

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import org.ascii.asciiPayCompanion.R
import org.ascii.asciiPayCompanion.accountManagement.AccountDataManager

class LoginDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(R.layout.password_prompt)
            .setCancelable(true)
            // TODO add action
            .setPositiveButton(getString(R.string.LoginOption)) { _, _ ->
                val dialog = dialog ?: run { throw UnknownError() }
                val username = dialog.findViewById<TextInputEditText>(R.id.AccountEmail).text.toString()
                val password = dialog.findViewById<TextInputEditText>(R.id.AccountPassword).text.toString()
//                AccountDataManager.login(username, password)
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .create()
    }

    companion object {
        const val TAG = "LoginDialog"
    }

}