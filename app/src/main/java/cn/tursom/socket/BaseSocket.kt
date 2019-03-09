package cn.tursom.socket

import cn.tursom.tools.*
import org.apache.commons.lang3.SerializationUtils.serialize
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * 对基础的Socket做了些许封装
 */
@Suppress("unused")
open class BaseSocket(
	private val socket: Socket,
	private val timeout: Int = Companion.timeout
) : Closeable {
	
	val address = socket.inetAddress?.toString()?.drop(1) ?: "0.0.0.0"
	val port by lazy { socket.port }
	val localPort by lazy { socket.localPort }
	private val inputStream by lazy { socket.getInputStream()!! }
	private val outputStream by lazy { socket.getOutputStream()!! }
	
	fun send(message: String?) {
		send((message ?: return).toByteArray())
	}
	
	fun send(message: ByteArray?) {
		outputStream.write(message ?: return)
	}
	
	fun send(message: Int) {
		send(message.toByteArray())
	}
	
	fun send(message: Long) {
		send(message.toByteArray())
	}
	
	fun sendObject(obj: Any?): Boolean {
		send(serialize(obj ?: return false) ?: return false)
		return true
	}
	
	inline fun <reified T> recvObject(): T? {
		return try {
			unSerialize(recv()) as T
		} catch (e: Exception) {
			null
		}
	}
	
	fun recvString(
		readTimeout: Int = 100,
		firstTimeout: Int = timeout
	): String {
		return recv(readTimeout, firstTimeout).toUTF8String()
	}
	
	fun recvInt(
		timeout1: Int = timeout,
		exception: Exception.() -> Int? = { printStackTrace();null },
		timeoutException: SocketTimeoutException.() -> Int? = { null }
	): Int? {
		return try {
			val buffer = ByteArray(4)
			socket.soTimeout = timeout1
			var sTime = System.currentTimeMillis()
			//读取数据
			var rSize = inputStream.read(buffer, 0, 4)
			while (rSize < 4) {
				val sTime2 = System.currentTimeMillis()
				socket.soTimeout -= (sTime2 - sTime).toInt()
				sTime = sTime2
				val sReadSize = inputStream.read(buffer, rSize, 8 - rSize)
				if (sReadSize <= 0) {
					break
				} else {
					rSize += sReadSize
				}
			}
			buffer.toInt()
		} catch (e: SocketTimeoutException) {
			e.timeoutException()
		} catch (e: Exception) {
			e.exception()
		}
	}
	
	fun recvLong(
		timeout1: Int = timeout,
		exception: Exception.() -> Long? = { printStackTrace();null },
		timeoutException: SocketTimeoutException.() -> Long? = { null }
	): Long? {
		return try {
			val buffer = ByteArray(8)
			socket.soTimeout = timeout1
			var sTime = System.currentTimeMillis()
			//读取数据
			var rSize = inputStream.read(buffer, 0, 8)
			while (rSize < 4) {
				val sTime2 = System.currentTimeMillis()
				socket.soTimeout -= (sTime2 - sTime).toInt()
				sTime = sTime2
				val sReadSize = inputStream.read(buffer, rSize, 8 - rSize)
				if (sReadSize <= 0) {
					break
				} else {
					rSize += sReadSize
				}
			}
			buffer.toLong()
		} catch (e: SocketTimeoutException) {
			e.timeoutException()
		} catch (e: Exception) {
			e.exception()
		}
	}
	
	fun recv(
		readTimeout: Int = 100,
		waitTimeout: Int = timeout
	): ByteArray {
		val buffer = ByteArrayOutputStream()
		recv(buffer, readTimeout, waitTimeout)
		return buffer.toByteArray()
	}
	
	fun recv(
		buffer: ByteArrayOutputStream,
		readTimeout: Int = 100,
		waitTimeout: Int = timeout
	) = try {
		val buf = ByteArray(1024)
		socket.soTimeout = waitTimeout
		var readSize = inputStream.read(buf)
		socket.soTimeout = readTimeout
		while (readSize > 0) {
			buffer.write(buf, 0, readSize)
			readSize = inputStream.read(buf)
		}
	} catch (e: SocketTimeoutException) {
	}
	
	override fun close() {
		closeSocket()
	}
	
	protected fun closeSocket() {
		if (!socket.isClosed) {
			closeInputStream()
			closeOutputStream()
			socket.close()
		}
	}
	
	private fun closeInputStream() {
		try {
			inputStream.close()
		} catch (e: Exception) {
		}
	}
	
	private fun closeOutputStream() {
		try {
			outputStream.close()
		} catch (e: Exception) {
		}
	}
	
	fun isConnected(): Boolean {
		return socket.isConnected
	}
	
	companion object Companion {
		const val defaultReadSize: Int = 1024 * 8
		const val timeout: Int = 60 * 1000
		
	}
}