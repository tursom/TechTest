package cn.tursom.techtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import cn.tursom.android.client.SocketConnect
import cn.tursom.socket.client.NioClient
import cn.tursom.socket.server.MultithreadingSocketServer
import cn.tursom.socket.server.SocketServer
import cn.tursom.tools.getTAG
import cn.tursom.tools.getWIFILocalIpAddress
import cn.tursom.tools.toUTF8String
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class NetworkTestActivity : AppCompatActivity() {
	private lateinit var networkTestShowTextView: TextView
	private lateinit var printScrollView: ScrollView
	private var socketServer: SocketServer? = null
	private var nioClient: NioClient? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
//		setContentView(R.layout.activity_network_test)
		
		//初始化view变量
		initView()
		
		//输出ready提示已完成准备工作
		println("ready")
	}
	
	
	/**
	 * 复原屏幕内容
	 */
	override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
		Log.d(TAG, "onRestoreInstanceState:")
		super.onRestoreInstanceState(savedInstanceState)
		savedInstanceState?.getString("text")?.let {
			networkTestShowTextView.text = it
		}
	}
	
	/**
	 * 保存屏幕内容
	 */
	override fun onSaveInstanceState(outState: Bundle?) {
		Log.d(TAG, "onSaveInstanceState:")
		super.onSaveInstanceState(outState)
		outState?.putString("text", networkTestShowTextView.text.toString())
	}
	
	fun connectToServer(host: String, port: Int) {
		GlobalScope.launch(Dispatchers.IO) {
			try {
				//先关闭已有连接
				nioClient?.close()
				nioClient = NioClient(host, port) { msg ->
					println(
						"Test Client: receive from $address count ${msg.count()}:\n" +
								"${msg.toUTF8String()}\n" +
								"===================="
					)
					read()
				}
				nioClient!!.read()
				println("connecting to server: ${nioClient?.address}")
			} catch (e: Throwable) {
				e.printStackTrace()
				println("can't connecting to server: $host:$port")
			}
		}
	}
	
	override fun onDestroy() {
		Log.d(TAG, "onDestroy:")
		super.onDestroy()
		nioClient?.close()
		socketServer?.close()
	}
	
	/**
	 * 初始化view相关变量
	 */
	private fun _LinearLayout.initOutputScroll() {
		printScrollView = scrollView { networkTestShowTextView = textView() }
			.lparams(width = matchParent, height = dip(0), weight = 1.0f)
	}
	
	private fun _LinearLayout.initServiceFunction() {
		horizontalScrollView().lparams(width = matchParent, height = wrapContent).linearLayout {
			button("清屏") { onClick { networkTestShowTextView.text = "" } }
			val networkTestGetWeatherEditText = editText { hint = "城市" }
			button("获取天气").onClick {
				val city = networkTestGetWeatherEditText.text.toString()
				networkTestGetWeatherEditText.setText("")
				if (city.count() == 0) return@onClick
				getWeather(city)
			}
			val port = editText {
				hint = "服务端口"
				inputType = InputType.TYPE_CLASS_NUMBER
			}
			button("启动测试服务器").onClick {
				val portStr = port.text.toString()
				if (portStr.isEmpty()) {
					println("port is null")
				} else {
					val port = portStr.toIntOrNull() ?: return@onClick
					startTestServer(port)
					connectToServer("127.0.0.1", port)
				}
			}
			button("关闭测试服务器").onClick {
				socketServer?.close()
				println("Test Server: server closed")
			}
			button("WiFi IP").onClick {
				println("WiFi IP address: ${getWIFILocalIpAddress(this@NetworkTestActivity)}")
			}
		}
	}
	
	private fun _LinearLayout.initClientFunction() {
		horizontalScrollView().lparams(width = matchParent, height = wrapContent).linearLayout {
			val msg = editText { hint = " 发送信息 " }
			button("发送").onClick {
				nioClient?.execute {
					val message = msg.text.toString().toByteArray()
					println("Test Client: send message to ${nioClient?.address} count ${message.count()}")
					write(message)
				}
			}
			val host = editText { hint = "地址" }
			val port = editText {
				hint = "端口"
				inputType = InputType.TYPE_CLASS_NUMBER
			}
			button("连接").onClick {
				try {
					//获取要连接的服务器的地址和端口并检验
					val serverAddress = host.text.toString()
					val serverPort = port.text.toString()
					if (serverAddress.count() == 0) {
						println("Test Client: server address is null")
					}
					if (serverPort.count() == 0) {
						println("Test Client: server port is null")
					}
					connectToServer(serverAddress, serverPort.toInt())
				} catch (e: Throwable) {
					println("连接错误: ${e.message}")
					println(e.getStackTraceString())
				}
			}
		}
	}
	
	private fun initView() {
		verticalLayout {
			initOutputScroll()
			initServiceFunction()
			initClientFunction()
		}
	}
	
	private fun startTestServer(port: Int) {
		GlobalScope.launch(Dispatchers.IO) {
			try {
				socketServer?.close()
				socketServer = MultithreadingSocketServer(port, 4) {
					this@NetworkTestActivity.println("Test Server: connection from $address")
					while (true) {
						val recv = recvString()
						if (recv.isEmpty()) break
						println(
							"Test Server: recv from $address:\n" +
									"$recv\n" +
									"===================="
						)
						send(recv)
					}
				}
				socketServer!!.run()
			} catch (e: Throwable) {
				println("local server start error: ${e.message}")
				println(e.getStackTraceString())
			}
		}
		println("server running in ${getWIFILocalIpAddress(this)}:$port")
	}
	
	/**
	 * 获取天气
	 * @param city 城市名
	 */
	private fun getWeather(city: String) {
		Log.d(TAG, "getWeather:")
		//连接到服务器
		val net = SocketConnect("tursom.cn", 15432)
		//添加任务
		net.execute {
			println("getWeather: connected to ${net.address}")
			net.send(city)
			val recv = net.recv(10240) ?: "null"
			println(recv)
			println("getWeather: end")
			//关闭连接
			net.close()
		}
	}
	
	private fun println(any: Any) = println(any.toString())
	private fun println(text: String = "") = runOnUiThread {
		networkTestShowTextView.append(text)
		networkTestShowTextView.append("\n")
		printScrollView.post { printScrollView.smoothScrollTo(0, networkTestShowTextView.bottom) }
	}
	
	private fun print(any: Any) = print(any.toString())
	private fun print(text: String) {
		runOnUiThread {
			networkTestShowTextView.append(text)
			printScrollView.post { printScrollView.smoothScrollTo(0, networkTestShowTextView.bottom) }
		}
	}
	
	private fun <T : View> Int.view() = findViewById<T>(this)!!
	
	/**
	 * 伴生对象
	 */
	companion object {
		val TAG = getTAG(this::class.java)
	}
}
