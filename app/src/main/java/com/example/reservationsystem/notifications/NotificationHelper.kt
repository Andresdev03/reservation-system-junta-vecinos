package com.example.reservationsystem.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.reservationsystem.R

object NotificationHelper {

    private const val CHANNEL_ID = "reservas_channel"
    private const val NOTIFICATION_ID = 1001

    @RequiresApi(Build.VERSION_CODES.O)
    fun crearCanal(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reservas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones sobre estado de reservas"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun mostrarNotificacionReserva(
        context: Context,
        titulo: String,
        mensaje: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}