@file:Suppress("unused")

package cn.tursom.tools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.support.annotation.IdRes
import android.support.annotation.IntRange
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


fun formatIpAddress(ip: Int): String {
//			val bytes = BigInteger.valueOf(ip.toLong()).toByteArray()
//			return InetAddress.getByAddress(bytes).hostAddress
	return "${ip and 0xff}.${(ip shr 8) and 0xff}.${(ip shr 16) and 0xff}.${(ip shr 24) and 0xff}"
}

fun getWIFILocalIpAddress(mContext: Context, errorCode: String = "Wifi已关闭"): String {
	//获取wifi服务
	val wifiManager = mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
	//判断wifi是否开启
	if (!wifiManager.isWifiEnabled) return errorCode
	val wifiInfo = wifiManager.connectionInfo
	val ipAddress = wifiInfo.ipAddress
	return formatIpAddress(ipAddress)
}

var EditText.sText: String
	get() = text.toString()
	set(value) = setText(value)

fun ByteArray.toUTF8String() = String(this, Charsets.UTF_8)

fun String.base64() = this.toByteArray().base64().toUTF8String()

fun ByteArray.base64(): ByteArray {
	return Base64.getEncoder().encode(this)
}

fun String.base64decode() = Base64.getDecoder().decode(this).toUTF8String()

fun ByteArray.base64decode(): ByteArray = Base64.getDecoder().decode(this)

fun AppCompatActivity.hideTitle() = this.supportActionBar?.hide()

fun Context.startActivity(cls: Class<*>) {
	startActivity(
		Intent(this, cls)
	)
}

fun Activity.openWebPage(url: String) {
	val uri = Uri.parse(url)
	val intent = Intent(Intent.ACTION_VIEW, uri)
	startActivity(intent)
}

fun String.md5() = digest("MD5")
fun ByteArray.md5() = digest("MD5")
fun String.md5OrNull() = digestOrNull("MD5")
fun ByteArray.md5OrNull() = digestOrNull("MD5")

fun String.sha() = digest("SHA")
fun ByteArray.sha() = digest("SHA")
fun String.shaOrNull() = digestOrNull("SHA")
fun ByteArray.shaOrNull() = digestOrNull("SHA")

fun String.sha1() = digest("SHA-1")
fun ByteArray.sha1() = digest("SHA-1")
fun String.sha1OrNull() = digestOrNull("SHA-1")
fun ByteArray.sha1OrNull() = digestOrNull("SHA-1")

fun String.sha256() = digest("SHA-256")
fun ByteArray.sha256() = digest("SHA-256")
fun String.sha256OrNull() = digestOrNull("SHA-256")
fun ByteArray.sha256OrNull() = digestOrNull("SHA-256")

fun String.sha384() = digest("SHA-384")
fun ByteArray.sha384() = digest("SHA-384")
fun String.sha384OrNull() = digestOrNull("SHA-384")
fun ByteArray.sha384OrNull() = digestOrNull("SHA-384")

fun String.sha512() = digest("SHA-512")
fun ByteArray.sha512() = digest("SHA-512")
fun String.sha512OrNull() = digestOrNull("SHA-512")
fun ByteArray.sha512OrNull() = digestOrNull("SHA-512")

fun String.digest(algorithm: String) = toByteArray().digest(algorithm).toHexString()
fun ByteArray.digest(algorithm: String) = digestOrNull(algorithm)!!
fun String.digestOrNull(algorithm: String) = toByteArray().digestOrNull(algorithm)?.toHexString()
fun ByteArray.digestOrNull(algorithm: String): ByteArray? {
	return try {
		//获取md5加密对象
		val instance = MessageDigest.getInstance(algorithm)
		//对字符串加密，返回字节数组
		instance.digest(this)
	} catch (e: NoSuchAlgorithmException) {
		e.printStackTrace()
		null
	}
}

fun ByteArray.toHexString(): String {
	val sb = StringBuffer()
	forEach {
		//获取低八位有效值+
		val i: Int = it.toInt() and 0xff
		//将整数转化为16进制
		var hexString = Integer.toHexString(i)
		if (hexString.length < 2) {
			//如果是一位的话，补0
			hexString = "0$hexString"
		}
		sb.append(hexString)
	}
	return sb.toString()
}

fun randomInt(@IntRange(from = 0) min: Int, @IntRange(from = 0) max: Int) =
	Random().nextInt(max) % (max - min + 1) + min

//fun makeToast(activity: Activity, message: String) =
//		Toast.makeText(
//				activity,
//				message,
//				Toast.LENGTH_SHORT
//		).show()

fun Activity.makeToast(message: String) {
	GlobalScope.launch(Dispatchers.Main) {
		Toast.makeText(this@makeToast, message, Toast.LENGTH_SHORT).show()
	}
}

fun getTAG(cls: Class<*>): String {
	return cls.name.split(".").last().dropLast(10)
}