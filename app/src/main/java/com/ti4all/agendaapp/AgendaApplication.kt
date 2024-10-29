package com.ti4all.agendaapp

import android.app.Application
import androidx.room.Room

class AgendaApplication : Application() {

    lateinit var database : AgendaDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this, AgendaDatabase::class.java
            , "agenda-db")
            .build()
    }

    companion object {
        lateinit var instance: AgendaApplication
            private set
    }
}