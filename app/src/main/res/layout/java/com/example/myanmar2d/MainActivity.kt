class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlarmScheduler.scheduleAll(this)
        
        // Notification Service ကို တန်းစတင်ခြင်း
        startService(Intent(this, FloatingTickerService::class.java))

        setContent {
            MaterialTheme {
                AppRoot()
            }
        }
    }
}
