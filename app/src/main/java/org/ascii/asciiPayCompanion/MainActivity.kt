package org.ascii.asciiPayCompanion

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pm : PackageManager = this.packageManager

        val hasNFC = pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)

        if (!hasNFC) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(resources.getString(R.string.hceUnavailableTitle))
            alertDialog.setMessage(resources.getString(R.string.hceUnavailableMessage))
            alertDialog.setNeutralButton(android.R.string.ok, DialogInterface.OnClickListener()
                {_, _ -> finishAndRemoveTask()})
            alertDialog.show()
        }

    }
}