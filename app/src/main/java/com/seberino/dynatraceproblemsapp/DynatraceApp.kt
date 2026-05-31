package com.seberino.dynatraceproblemsapp

import android.app.Application
import com.seberino.dynatraceproblemsapp.data.Graph

class DynatraceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}
