package org.ascii.asciiPayCompanion

import android.app.Application
import android.content.Context

// this seems a little bit like a hack
// The way this is done is described here:
// https://stackoverflow.com/questions/7144177/getting-the-application-context
class App : Application() {
    companion object{
        lateinit var appContext : Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}