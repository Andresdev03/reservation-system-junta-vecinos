package com.example.reservationsystem

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.reservationsystem.data.local.AppDatabase
import com.example.reservationsystem.data.repository.AppRepository
import com.example.reservationsystem.ui.screens.resumen.InicioScreen
import com.example.reservationsystem.ui.theme.ReservationSystemTheme
import com.example.reservationsystem.viewmodel.MainViewModel
import com.example.reservationsystem.viewmodel.MainViewModelFactory
import com.example.reservationsystem.notifications.NotificationHelper
import com.example.reservationsystem.ui.navigation.AppNavigation
import com.example.reservationsystem.ui.navigation.AppRoot

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "gestion_vecinal_db"
        ).fallbackToDestructiveMigration().build()

        val repository = AppRepository(
            movimientoDao = db.movimientoDao(),
            reservaDao = db.reservaDao()
        )

        NotificationHelper.crearCanal(this)

        setContent {
            ReservationSystemTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(repository)
                )
                AppRoot(mainViewModel = viewModel)
            }
        }
    }
}