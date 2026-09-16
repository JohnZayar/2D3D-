package com.example.myanmar2d

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class FloatingTickerService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val CHANNEL_ID = "FloatingTickerChannel"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Myanmar 2D Live")
            .setContentText("Floating ticker is running live...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        // Foreground Service အဖြစ် သတ်မှတ်ခြင်း (တခြား App တွေဖွင့်လည်း လုံးဝမပိတ်တော့ပါ)
        startForeground(1, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_ticker_layout, null)

        try {
            windowManager.addView(floatingView, params)
            startLiveTickerLoop(floatingView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Floating Ticker Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun startLiveTickerLoop(view: View) {
        val tickerTextView = view.findViewById<TextView>(R.id.floatingTickerText)

        serviceScope.launch {
            while (true) {
                try {
                    when (val result = SettradeRepository.fetchLiveSetIndex()) {
                        is SettradeRepository.FetchResult.Success -> {
                            val setText = "%.2f".format(result.data.set)
                            val valText = "%,.2f".format(result.data.value)
                            val twoDText = result.data.twoD
                            
                            tickerTextView.text = "SET: $setText  |  Val: $valText  |  2D: $twoDText  |  Thai: 417212"
                        }
                        is SettradeRepository.FetchResult.Failure -> {
                            tickerTextView.text = "Reconnecting Live Data..."
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(4000) // ၄ စက္ကန့်တစ်ကြိမ် Live Update
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
