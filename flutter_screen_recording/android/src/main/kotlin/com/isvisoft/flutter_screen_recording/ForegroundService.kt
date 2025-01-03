package com.foregroundservice

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.isvisoft.flutter_screen_recording.FlutterScreenRecordingPlugin
import com.isvisoft.flutter_screen_recording.R
import android.app.Activity

class ForegroundService : Service() {
    private val CHANNEL_ID = "ForegroundService Kotlin"
    private val REQUEST_CODE_MEDIA_PROJECTION = 1001

    companion object {
        fun startService(context: Context, title: String, message: String) {
            println("-------------------------- startService");

            try {
                val startIntent = Intent(context, ForegroundService::class.java)
                startIntent.putExtra("messageExtra", message)
                startIntent.putExtra("titleExtra", title)
                println("-------------------------- startService2");

                ContextCompat.startForegroundService(context, startIntent)
                println("-------------------------- startService3");

            } catch (err: Exception) {
                println("startService err");
                println(err);
            }
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {

            println("-------------------------- onStartCommand")

            // Verificar permisos en Android 14 (SDK 34)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION)
                    != PackageManager.PERMISSION_GRANTED) {
                    println("MediaProjection permission not granted, requesting permission")

                    // Solicitar el permiso si no ha sido concedido
                    ActivityCompat.requestPermissions(
                        this as Activity,
                        arrayOf(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION),
                        REQUEST_CODE_MEDIA_PROJECTION
                    )
                } else {
                    // Si ya está concedido, continuar normalmente
                    startForegroundServiceWithNotification(intent)
                }
            } else {
                // Si no es Android 14, continuar normalmente
                startForegroundServiceWithNotification(intent)
            }

            return START_NOT_STICKY
        } catch (err: Exception) {
            println("onStartCommand err")
            println(err)
        }
        return START_STICKY
    }

private fun startForegroundServiceWithNotification(intent: Intent?) {
    val title = intent?.getStringExtra("titleExtra") ?: "Flutter Screen Recording"
    val message = intent?.getStringExtra("messageExtra") ?: ""

    createNotificationChannel()

    val notificationIntent = Intent(this, FlutterScreenRecordingPlugin::class.java)

    val pendingIntent = PendingIntent.getActivity(
        this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(android.R.drawable.ic_media_play) // Use a system icon
        .setContentIntent(pendingIntent)
        .build()

    startForeground(1, notification)
    println("-------------------------- startForegroundServiceWithNotification")
}

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}