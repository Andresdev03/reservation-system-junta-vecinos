package com.example.reservationsystem.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.reservationsystem.data.local.dao.MovimientoDao
import com.example.reservationsystem.data.local.dao.ReservaDao
import com.example.reservationsystem.data.local.entity.MovimientoEntity
import com.example.reservationsystem.data.local.entity.ReservaEntity

@Database(
    entities = [MovimientoEntity::class, ReservaEntity::class],
    version = 4,
    exportSchema = false
)

@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun movimientoDao(): MovimientoDao
    abstract fun reservaDao(): ReservaDao
}