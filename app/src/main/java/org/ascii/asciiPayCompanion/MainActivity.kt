package org.ascii.asciiPayCompanion

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pm : PackageManager = this.packageManager
        Log.e("Ascii Companion App", "Starting up!")

        val hasNFC = pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
        if (!hasNFC) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(resources.getString(R.string.hceUnavailableTitle))
            alertDialog.setMessage(resources.getString(R.string.hceUnavailableMessage))
            alertDialog.setNeutralButton(android.R.string.ok, DialogInterface.OnClickListener()
            {_, _ -> })
            alertDialog.show()
        }
        // createDummyCard()

        // visual representation of the card
        val cardSP = getSharedPreferences("card", MODE_PRIVATE)
        val fullName = cardSP.getString("full_name", null)
        val cardId = cardSP.getString("id", null)
        if(fullName!=null && cardId!=null){
            val cardText = findViewById<TextView>(R.id.cardInformation)
            cardText.text = getString(R.string.visualCardFormat, fullName, cardId)
        }


    }

    private fun createDummyCard() {
        // TODO replace this method with proper way of instantiating the card data
        val cardSP = getSharedPreferences("card", MODE_PRIVATE)
        val cardEditor = cardSP.edit()
        cardEditor.putString("id", "AFFE1337C0FFEE00")
        cardEditor.putString("key", "7665165AADE654654AACC112131415161718192021222324")
        cardEditor.putString("full_name", "Peter Zwegat")
        cardEditor.apply()
    }
}