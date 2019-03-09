package cn.tursom.techtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.tursom.tools.startActivity
import org.jetbrains.anko.button
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.verticalLayout

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		scrollView().verticalLayout {
			button("传感器测试").onClick { startActivity(SensorActivity::class.java) }
			button("网络传输测试").onClick { startActivity(NetworkTestActivity::class.java) }
			button("加密测试").onClick { startActivity(EncryptionActivity::class.java) }
			button("二维码测试").onClick { startActivity(QRCodeTestActivity::class.java) }
			button("课表推送控制器").onClick { startActivity(ClassScheduleControllerActivity::class.java) }
		}
	}
}
