package org.ascii.asciiPayCompanion

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    val cardView: CardView? = findViewById(R.id.cardVisual)
    private val accountManager = AccountManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("Ascii Companion App", "Starting up!")

        val hasNFC = this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
        if (!hasNFC) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(resources.getString(R.string.hceUnavailableTitle))
            alertDialog.setMessage(resources.getString(R.string.hceUnavailableMessage))
            alertDialog.setNeutralButton(android.R.string.ok, DialogInterface.OnClickListener()
            {_, _ -> })
            alertDialog.show()
        }

        // this will cause our visual representation to be shown,
        //  which is why it is done relatively late
        accountManager.registerAccountUser(AccountListener())
    }

    inner class AccountListener : AccountUser{
        override fun onAccountChange(account: Account?) {
            account?.let { account ->
                cardView?.let {
                    cardView.visibility = View.VISIBLE
                    // decide whether to show a virtual representation of the card
                    findViewById<TextView>(R.id.cardInformation)?.let { textView ->
                        // TODO use another field than the (very technical) nfc ID
                        val cardId = account.card_data?.id ?: String()
                        textView.text = getString(R.string.visualCardFormat, account.name, cardId)
                    }
                }
            } ?:
            // disable the card view if no account data is exiting
            let {
                cardView?.let {
                    cardView.visibility = View.GONE
                }
            }
        }
    }
}