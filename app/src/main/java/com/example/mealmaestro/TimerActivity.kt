package com.example.mealmaestro


import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity

open class TimerActivity(private val millisInFuture : Long, private val countDownInterval : Long) {

    private val millisUntilFinished = millisInFuture
    private var timer = InternalTimer(this,millisInFuture, countDownInterval)
    private var isRunning = false
    var onTick: ((millisUntilFinished:Long) -> Unit)? = null
    var onFinish : (() -> Unit)? = null

    private class InternalTimer(private val parent : TimerActivity, millisInFuture: Long, countDownInterval: Long): CountDownTimer(millisInFuture, countDownInterval) {

        var millisUntilFinished = parent.millisUntilFinished
        override fun onTick(millisUntilsFinished: Long) {
            this.millisUntilFinished = millisUntilsFinished
            parent.onTick?.invoke(millisUntilsFinished)
        }

        override fun onFinish() {
            millisUntilFinished = 0
            parent.onFinish?.invoke()
        }
    }

    fun pauseTimer(){
        timer.cancel()
        isRunning = false
    }

    fun resumeTimer(){
        if (!isRunning && timer.millisUntilFinished > 0){
            timer = InternalTimer(this, timer.millisUntilFinished, countDownInterval)
            startTimer()
        }
    }

    fun startTimer(){
        timer.start()
        isRunning = true
    }

    fun restartTimer(){
        timer.cancel()
        timer = InternalTimer(this, millisUntilFinished, countDownInterval)
        startTimer()
    }

    fun destroyTimer(){
        timer.cancel()
    }

}