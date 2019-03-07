package cn.tursom.techtest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.tursom.tools.makeToast
import cn.tursom.tools.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View?) {
        when (view?.id) {
            R.id.mainSensorTestButton -> startActivity(SensorActivity::class.java)
            R.id.mainNetworkTestButton -> startActivity(NetworkTestActivity::class.java)
            R.id.mainEncryptionButton -> startActivity(EncryptionActivity::class.java)
            R.id.mainQRCodeTest -> startActivity(QRCodeTestActivity::class.java)
            R.id.mainClassScheduleController -> startActivity(ClassScheduleControllerActivity::class.java)
            else -> makeToast("无法找到对应的界面")
        }
    }
}
