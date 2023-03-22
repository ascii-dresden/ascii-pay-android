package org.ascii.asciiPayCompanion.UI

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.ascii.asciiPayCompanion.R
import org.ascii.asciiPayCompanion.Utils.Companion.toHex
import org.ascii.asciiPayCompanion.accountManagement.AccountDataManager
import org.ascii.asciiPayCompanion.accountManagement.AccountSession

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("Ascii Companion App", "Starting up!")
        AccountDataManager.registerAccountUser(AccountListener())
        val addCardPreview = findViewById<CardView>(R.id.addCardPreview)
        addCardPreview.setOnClickListener{
            LoginDialog().show(supportFragmentManager, LoginDialog.TAG)
        }

        val pm = this.packageManager
        val hasNFC = pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
        if (!hasNFC) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(resources.getString(R.string.hceUnavailableTitle))
            alertDialog.setMessage(resources.getString(R.string.hceUnavailableMessage))
            alertDialog.setNeutralButton(android.R.string.ok, DialogInterface.OnClickListener()
            {_, _ -> })
            alertDialog.show()
        }
    }

    inner class AccountListener : AccountDataManager.AccountUser {
        override fun onAccountChange(accountSession: AccountSession?) {
            val cardView: CardView? = findViewById(R.id.cardVisual)
            accountSession?.let { account ->
                cardView?.let {
                    cardView.visibility = View.VISIBLE
                    // decide whether to show a virtual representation of the card
                    findViewById<TextView>(R.id.cardInformation)?.let { textView ->
                        // TODO use another field than the (very technical) nfc ID
                        val cardId = toHex(account.cardData.id)
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