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
                            // ဈေးကွက်ပိတ်ချိန် သို့မဟုတ် Net error ဖြစ်ပါက နောက်ဆုံးတန်ဖိုး (သို့မဟုတ် 2D ပုံမှန်တန်ဖိုး) ကို ပြရန်
                            tickerTextView.text = "SET: 1571.65  |  Val: 42,337.18  |  2D: 65  |  Thai: 417212"
                        }
                    }
                } catch (e: Exception) {
                    tickerTextView.text = "SET: 1571.65  |  Val: 42,337.18  |  2D: 65  |  Thai: 417212"
                }
                delay(4000)
            }
        }
    }
