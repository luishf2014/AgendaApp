package com.ti4all.agendaapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ti4all.agendaapp.dao.AgendaDao
import com.ti4all.agendaapp.data.Agenda

@Database(entities = [Agenda::class], version = 1) // Mantendo a versão 2
abstract class AgendaDatabase : RoomDatabase() {

    abstract fun agendaDao(): AgendaDao

}
