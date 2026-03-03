package com.example.marsphotos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.marsphotos.MainActivity
import com.example.marsphotos.R

/**
 * NotificationHelper - Gestiona las notificaciones de la aplicación
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_GRADES = "grades_channel"
        const val CHANNEL_NAME_GRADES = "Nuevas Calificaciones"
        const val CHANNEL_DESCRIPTION_GRADES = "Notificaciones cuando se reciben nuevas calificaciones"
        const val NOTIFICATION_ID_NEW_GRADE = 1001
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannels()
    }

    /**
     * Crea los canales de notificación (requerido para Android 8.0+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_GRADES,
                CHANNEL_NAME_GRADES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION_GRADES
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Muestra una notificación de nueva calificación
     */
    fun showNewGradeNotification(materia: String, calificacion: String, tipo: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "grades")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GRADES)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("¡Nueva calificación!")
            .setContentText("$materia - $tipo: $calificacion")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Se ha registrado una nueva calificación en $materia\n$tipo: $calificacion"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_NEW_GRADE + materia.hashCode(), notification)
    }

    /**
     * Muestra notificación de calificaciones actualizadas
     */
    fun showGradesUpdatedNotification(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "grades")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GRADES)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Calificaciones actualizadas")
            .setContentText("Se han actualizado $count calificación(es)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_NEW_GRADE, notification)
    }
}
