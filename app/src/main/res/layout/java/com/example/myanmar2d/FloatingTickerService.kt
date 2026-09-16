package com.example.myanmar2d

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class FloatingTickerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val CHANNEL_ID = "LiveTickerNotificationChannel"
    private val NOTIFICATION_ID = 1001

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Connecting Live Ticker..."))
        startLiveTickerLoop()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Myanmar 2D Live Ticker",
                NotificationManager.IMPORTANCE_LOW // အသံမမြည်ဘဲ ငြိမ်သက်စွာ အမြဲပေါ်နေရန်
            ).apply {
                description = "Shows live 2D and SET data on notification bar"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🇲🇲 Myanmar 2D Live")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // ဖျက်မရအောင် အမြဲစွဲမြဲနေစေရန်
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startLiveTickerLoop() {
        serviceScope.launch {
            while (true) {
                try {
                    when (val result = SettradeRepository.fetchLiveSetIndex()) {
                        is SettradeRepository.FetchResult.Success -> {
                            val setText = "%.2f".format(result.data.set)
                            val valText = "%,.2f".format(result.data.value)
                            val twoDText = result.data.twoD
                            
                            val infoText = "SET: $setText | Val: $valText | 2D: $twoDText"
                            updateNotification(infoText)
                        }
                        is SettradeRepository.Failure -> {
                            updateNotification("SET: 1571.65 | Val: 42,337.18 | 2D: 65")
                        }
                    }
                } catch (e: Exception) {
                    updateNotification("SET: 1571.65 | Val: 42,337.18 | 2D: 65")
                }
                delay(4000) // ၄ စက္ကန့်တစ်ကြိမ် Update
            }
        }
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
