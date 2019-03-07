package cn.tursom.techtest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import cn.tursom.tools.*
import com.google.gson.Gson
import java.time.DayOfWeek
import kotlinx.coroutines.*


class ClassScheduleControllerActivity : AppCompatActivity() {
	private var url: String = ""
		set(value) {
			urlEditText.sText = value
			field = value
		}
	private lateinit var token: String
	private var loginState: Boolean? = null
		set(value) {
			if (field != value) {
				if (value == true) {
					urlState.setBackgroundResource(R.drawable.check)
				} else {
					urlState.setBackgroundResource(R.drawable.close)
				}
				field = value
			}
		}
	
	private lateinit var urlState: ImageView
	private lateinit var urlEditText: EditText
	private lateinit var userEditText: EditText
	private lateinit var passwordEditText: EditText
	
	
	private val onTextChange = object : TextWatcher {
		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
		override fun afterTextChanged(s: Editable?) {}
		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
			loginState = false
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_class_schedule_controller)
		
		Log.d(TAG, "onCreate")
		initView()
		restoreInstanceState(savedInstanceState)
	}
	
	private fun restoreInstanceState(savedInstanceState: Bundle?) {
		Log.d(TAG, "onRestoreInstanceState")
		
		savedInstanceState?.run {
			loginState = getBoolean("loginState", false)
			if (loginState == true) {
				getString("url", null)?.run { url = this }
				getString("token", null)?.run { token = this }
			}
		}
		
		getSharedPreferences("loginData")?.also {
			if (loginState != true) {
				it["url"]?.let { url ->
					urlEditText.sText = url
					it["token"]?.let { token ->
						this.url = url
						this.token = token
						testToken { tokenState ->
							if (tokenState) {
								this.loginState = true
							}
						}
					}
				}
			}
			it["user"]?.run { userEditText.sText = this }
			it["password"]?.let { password -> passwordEditText.sText = password.md5().take(6) }
		}
	}
	
	private fun initView() {
		urlState = findViewById(R.id.classScheduleControllerUrlState)
		urlEditText = findViewById(R.id.classScheduleControllerUrl)
		userEditText = findViewById(R.id.classScheduleControllerUser)
		passwordEditText = findViewById(R.id.classScheduleControllerPassword)
		
		urlEditText.addTextChangedListener(onTextChange)
		userEditText.addTextChangedListener(onTextChange)
		passwordEditText.addTextChangedListener(onTextChange)
	}
	
	override fun onSaveInstanceState(outState: Bundle?) {
		Log.d(
			TAG,
			"onSaveInstanceState, ${if (loginState == true) "$loginState, $url, $token" else loginState?.toString()}"
		)
		super.onSaveInstanceState(outState)
		outState?.run {
			loginState?.let { state -> putBoolean("loginState", state) }
			if (loginState == true) {
				putString("url", url)
				putString("token", token)
			}
		}
	}
	
	fun clearMessage(view: View?) = when (view?.id) {
		R.id.classScheduleClearPassword -> passwordEditText.setText("")
		R.id.classScheduleClearUsername -> userEditText.setText("")
		R.id.classScheduleClearUrl -> urlEditText.setText("")
		else -> makeToast("无法找到对应组件")
	}
	
	
	fun loginTest(@Suppress("UNUSED_PARAMETER") view: View?) = login { _, _ -> }
	
	
	fun tokenTest(@Suppress("UNUSED_PARAMETER") view: View?) {
		GlobalScope.launch(Dispatchers.Main) {
			if (loginState != null) testToken {
				makeToast(if (it) "秘钥有效" else "秘钥失效")
			} else {
				makeToast("无法获得秘钥")
			}
		}
	}
	
	private fun getUrl(): String {
		return urlEditText.sText.let {
			if (it.last() == '/') {
				it
			} else {
				"$it/"
			}
		}.let {
			if (it[0] == 'h' && it[1] == 't' && it[2] == 't' && it[3] == 'p') {
				it
			} else {
				"http://$it"
			}
		}
	}
	
	private fun login(handler: (state: Boolean, code: String?) -> Unit) = GlobalScope.launch(Dispatchers.IO) {
		val url = getUrl()
		val user = userEditText.sText
		val pwd = passwordEditText.sText
		val sPwd = getSharedPreferences("loginData")["password"]
		Log.d(TAG, "password sha256: ${if (samePwd(sPwd, pwd)) sPwd else pwd.sha256()}")
		try {
			val loginState = Gson().fromJson(
				sendPost(
					url + "login.jsp", mapOf(
						Pair("username", user),
						Pair("password", if (samePwd(sPwd, pwd)) sPwd else pwd.sha256())
					), mapOf()
				),
				LoginStruct::class.java
			)
			if (loginState?.state == true) {
				this@ClassScheduleControllerActivity.url = url
				token = loginState.code!!
				this@ClassScheduleControllerActivity.loginState = true
				getSharedPreferences("loginData").save {
					this["url"] = url
					this["token"] = token
					this["user"] = user
					this["password"] = if (!samePwd(sPwd, pwd)) pwd.sha256() else sPwd
				}
			} else {
				this@ClassScheduleControllerActivity.loginState = false
			}
			Log.d(TAG, "login state: ${loginState?.state}, code: ${loginState?.code}")
			handler(loginState?.state ?: false, loginState?.code)
		} catch (e: Exception) {
			handler(false, "${e.javaClass}:${e.message}")
		}
	}
	
	/**
	 * 判断保存的密码与提供的密码是否一致
	 * @param saved 数据库里保存的密码
	 * @param password 提供的密码进行sha256摘要后的密码
	 *
	 * @return true 如果一致  false 如果不一致
	 */
	private fun samePwd(saved: String?, password: String?) = saved?.md5()?.take(6) == password
	
	/**
	 * 测试token是否有效
	 * @param handler 回调函数<hl>
	 *   回调函数的参数： true 如果token有效， false 如果token失效
	 *
	 * @return kotlin协程库的任务对象
	 */
	private fun testToken(handler: (state: Boolean) -> Unit) = GlobalScope.launch(Dispatchers.IO) {
		try {
			Gson().fromJson(
				sendPost(
					url + "run.jsp", mapOf(
						Pair("token", token),
						Pair("mod", "Echo"),
						Pair("message", "token test")
					), mapOf()
				),
				ResultStruct::class.java
			).let {
				loginState = it?.state ?: false
				Log.d(TAG, "token state: ${it?.state}, result: ${it?.result}")
				handler(it?.state ?: false)
			}
		} catch (e: Exception) {
			loginState = false
			handler(false)
		}
	}
	
	private fun SharedPreferences.save(handle: SharedPreferences.Editor.() -> Unit) = edit().run {
		handle()
		apply()
	}
	
	private fun allNotNull(vararg objects: Any?): Boolean {
		objects.forEach {
			if (it == null) return false
		}
		return true
	}
	
	private operator fun SharedPreferences.Editor.set(key: String, value: String) = putString(key, value)
	
	private operator fun SharedPreferences.get(key: String) = getString(key, null)
	
	private fun getSharedPreferences(name: String) = getSharedPreferences(name, Context.MODE_PRIVATE)
	
	data class LoginStruct(val state: Boolean?, val code: String?)
	data class ResultStruct(val state: Boolean?, val result: String?)
	data class ClassData(
		val name: String?,
		val location: String?,
		val time: Int = -1,
		val dayOfWeek: DayOfWeek,
		@Suppress("ArrayInDataClass") val weeks: List<Int>
	)
	
	data class EmailData(
		val host: String?,
		val port: Int?,
		val name: String?,
		val password: String?,
		val from: String?,
		val subject: String?
	)
	
	
	data class TokenData(val usr: String?) {
		companion object {
			fun getUser(token: String): String? {
				return try {
					Gson().fromJson(token.split('.')[1].base64decode(), TokenData::class.java).usr
				} catch (e: Exception) {
					null
				}
			}
		}
	}
	
	companion object {
		val TAG = getTAG(this::class.java)
	}
}
