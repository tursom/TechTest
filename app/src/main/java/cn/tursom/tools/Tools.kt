@file:Suppress("unused")

package cn.tursom.tools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.support.annotation.IntRange
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


fun formatIpAddress(ip: Int): String {
	return "${ip and 0xff}.${(ip shr 8) and 0xff}.${(ip shr 16) and 0xff}.${(ip shr 24) and 0xff}"
}

val Context.localIpv4Address: String?
	get() {
		//获取wifi服务
		val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
		//判断wifi是否开启
		return if (wifiManager.isWifiEnabled) {
			val wifiInfo = wifiManager.connectionInfo
			val ipAddress = wifiInfo.ipAddress
			formatIpAddress(ipAddress)
		} else {
			val en = NetworkInterface.getNetworkInterfaces()
			while (en.hasMoreElements()) {
				val networkInterface = en.nextElement()
				val enumIpAddress = networkInterface.inetAddresses
				while (enumIpAddress.hasMoreElements()) {
					val address = enumIpAddress.nextElement()
					if (!address.isLoopbackAddress && (address is Inet4Address)) {
						return address.hostAddress
					}
				}
			}
			null
		}
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

fun Int.toByteArray(): ByteArray {
	val array = ByteArray(4)
	array[0] = this.shr(3 * 8).toByte()
	array[1] = this.shr(2 * 8).toByte()
	array[2] = this.shr(1 * 8).toByte()
	array[3] = this.shr(0 * 8).toByte()
	return array
}

fun ByteArray.toInt(): Int =
	(this[0].toInt() shl 24) or
			(this[1].toInt() shl 16 and 0xff0000) or
			(this[2].toInt() shl 8 and 0xff00) or
			(this[3].toInt() and 0xFF)

fun Long.toByteArray(): ByteArray {
	val array = ByteArray(4)
	array[0] = this.shr(7 * 8).toByte()
	array[1] = this.shr(6 * 8).toByte()
	array[2] = this.shr(5 * 8).toByte()
	array[3] = this.shr(4 * 8).toByte()
	array[4] = this.shr(3 * 8).toByte()
	array[5] = this.shr(2 * 8).toByte()
	array[6] = this.shr(1 * 8).toByte()
	array[7] = this.shr(0 * 8).toByte()
	return array
}

fun ByteArray.toLong(): Long =
	(this[0].toLong() shl 56 and 0xff000000000000) or
			(this[1].toLong() shl 48 and 0xff0000000000) or
			(this[2].toLong() shl 40 and 0xff00000000) or
			(this[3].toLong() shl 32 and 0xff00000000) or
			(this[4].toLong() shl 24 and 0xff000000) or
			(this[5].toLong() shl 16 and 0xff0000) or
			(this[6].toLong() shl 8 and 0xff00) or
			(this[7].toLong() and 0xFF)

fun Int.left1(): Int {
	if (this == 0) {
		return -1
	}
	var exp = 4
	var pos = 1 shl exp
	while (exp > 0) {
		exp--
		if ((this shr pos) != 0) {
			pos += 1 shl exp
		} else {
			pos -= 1 shl exp
		}
	}
	return if (this shr pos != 0) pos else pos - 1
}

fun Long.left1(): Int {
	if (this == 0L) {
		return -1
	}
	var exp = 8
	var pos = 1 shl exp
	while (exp > 0) {
		exp--
		if ((this shr pos) != 0L) {
			pos += 1 shl exp
		} else {
			pos -= 1 shl exp
		}
	}
	return if (this shr pos != 0L) pos else pos - 1
}


/**
 * 序列化
 */
fun serialize(`object`: Any): ByteArray? = try {
	val baos = ByteArrayOutputStream()
	val oos = ObjectOutputStream(baos)
	oos.writeObject(`object`)
	baos.toByteArray()
} catch (e: Exception) {
	null
}

/**
 * 反序列化
 */
fun unSerialize(bytes: ByteArray): Any? = try {
	ObjectInputStream(ByteArrayInputStream(bytes)).readObject()
} catch (e: Exception) {
	null
}