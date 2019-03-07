@file:Suppress("DEPRECATION")

package cn.tursom.techtest

import android.annotation.SuppressLint
import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import cn.tursom.tools.getTAG
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class SensorActivity : AppCompatActivity(), SensorEventListener {
    /**
     * var bloc start
     */
    var times = 0
    var text = ""
    private lateinit var seniorTimesRecorderTextView: TextView
    private lateinit var sensorSeniorValueTextView: TextView
    private lateinit var sManager: SensorManager
    private lateinit var mSensorOrientation: Sensor
    private var onChangeTimes = 0
    private val scheduledThreadPool = object : ScheduledThreadPoolExecutor(3) {
        override fun remove(task: Runnable?): Boolean {
            Log.d(TAG, "scheduledThreadPool: remove")
            return super.remove(task)
        }

        override fun scheduleAtFixedRate(
            command: Runnable?,
            initialDelay: Long,
            period: Long,
            unit: TimeUnit?
        ): ScheduledFuture<*> {
            Log.d(TAG, "scheduledThreadPool: scheduleAtFixedRate")
            return super.scheduleAtFixedRate(command, initialDelay, period, unit)
        }
    }
    @SuppressLint("SetTextI18n")
    private val timeTask = Runnable {
        runOnUiThread {
            findViewById<TextView>(R.id.seniorTimesRecorderTextView).text = "click times:${times++}\n" +
                    "change times:$onChangeTimes"
            onChangeTimes = 0
            Log.d(TAG, "timing:$times")
        }
    }
    private var timeTaskFuture: ScheduledFuture<*>? = null
    /**
     * var block end
     */

    /**
     * function bloc start
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        text = savedInstanceState?.getString("text") ?: ""
        times = savedInstanceState?.getInt("times") ?: 0

        sManager = getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        mSensorOrientation = sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        sManager.registerListener(this, mSensorOrientation, SensorManager.SENSOR_DELAY_UI)
        bindViews()
    }


    private fun bindViews() {
        seniorTimesRecorderTextView = findViewById(R.id.seniorTimesRecorderTextView)

        sensorSeniorValueTextView = findViewById<View>(R.id.sensorSeniorValueTextView) as TextView
    }

    override fun onResume() {
        super.onResume()
        timeTaskFuture = scheduledThreadPool.scheduleAtFixedRate(timeTask, 0, 1, TimeUnit.SECONDS)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ${timeTaskFuture?.cancel(true)}")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("text", text)
        outState?.putInt("times", times)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        onChangeTimes++
        val sb = StringBuilder()
        if (event != null) {
            sb.append("方位角：")
            sb.append((Math.round(event.values[0] * 100)).toFloat() / 100)
        }
        sb.append("\n")
        if (event != null) {
            sb.append("倾斜角：")
            sb.append((Math.round(event.values[1] * 100)).toFloat() / 100)
        }
        sb.append("\n")
        if (event != null) {
            sb.append("滚动角：")
            sb.append((Math.round(event.values[2] * 100)).toFloat() / 100)
        }
        sensorSeniorValueTextView.text = sb.toString()
    }

    companion object {
        val TAG = getTAG(this::class.java)
    }
}
