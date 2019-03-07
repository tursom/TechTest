package cn.tursom.tools

import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


/**
 * 向指定URL发送GET方法的请求
 *
 * @param url
 * 发送请求的URL
 * @param param
 * 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
 * @return URL 所代表远程资源的响应结果
 */
fun sendGet(url: String, param: String, header: String?): String {
	var `in`: InputStreamReader? = null
	return try {
		val urlNameString = "$url?$param"
		val realUrl = URL(urlNameString)
		// 打开和URL之间的连接
		val connection = realUrl.openConnection()
		// 设置通用的请求属性
		connection.setRequestProperty("accept", "*/*")
		connection.setRequestProperty("connection", "Keep-Alive")
		connection.setRequestProperty(
			"user-agent",
			"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
		)
		if (header != null) {
			connection.addRequestProperty("Authorization", header)
		}
		// 建立实际的连接
		connection.connect()
		// 获取所有响应头字段
		//val map = connection.headerFields
		// 遍历所有的响应头字段
//			for (key in map.keys) {
//				println(key + "--->" + map[key])
//			}
		// 定义 BufferedReader输入流来读取URL的响应
		`in` = InputStreamReader(connection.getInputStream())
		`in`.readText()
	} catch (e: Exception) {
		e.printStackTrace()
		throw e
	} finally {
		try {
			`in`?.close()
		} catch (e2: Exception) {
			e2.printStackTrace()
		}
	}// 使用finally块来关闭输入流
}

/**
 * 向指定 URL 发送POST方法的请求
 *
 * @param url
 * 发送请求的 URL
 * @param param
 * 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
 * @return 所代表远程资源的响应结果
 */
fun sendPost(
	url: String, param: ByteArray? = null, header: Map<String, String> = mapOf(
		Pair("accept", "*/*"),
		Pair("connection", "Keep-Alive"),
		Pair("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
	)
): String {
	var out: OutputStream? = null
	var `in`: InputStreamReader? = null
	return try {
		val realUrl = URL(url)
		// 打开和URL之间的连接
		val conn = realUrl.openConnection() as HttpURLConnection

		conn.requestMethod = "POST"
		
		// 设置l请求属性
		header.forEach { key, value ->
			conn.setRequestProperty(key, value)
		}
		
		// 发送POST请求必须设置如下两行
		conn.doOutput = true
		conn.doInput = true
		
		param?.run param@{
			// 获取URLConnection对象对应的输出流
			out = conn.outputStream!!
			out?.run {
				write(this@param)
				flush()
			}
		}
		// 定义BufferedReader输入流来读取URL的响应
		`in` = InputStreamReader(conn.inputStream)
		`in`.readText()
	} catch (e: Exception) {
		throw e
	} finally {
		try {
			out?.close()
			`in`?.close()
		} catch (ex: IOException) {
			ex.printStackTrace()
		}
	}//使用finally块来关闭输出流、输入流
}

fun sendPost(
	url: String, param: Map<String, String>, header: Map<String, String> = mapOf(
		Pair("accept", "*/*"),
		Pair("connection", "Keep-Alive"),
		Pair("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
	)
): String {
	val paramStr = StringBuilder()
	param.forEach { key, value ->
		paramStr.append("${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}&")
	}
	if (paramStr.isNotEmpty()) paramStr.deleteCharAt(paramStr.length - 1)
	return sendPost(url, paramStr.toString().toByteArray(), header)
}