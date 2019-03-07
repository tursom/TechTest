package cn.tursom.android.client

import android.util.Log
import cn.tursom.tools.getTAG
import java.io.*
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.Executors


/**
 * Created by tursom on 2017/8/22.
 *
 *
 * class SocketConnect
 * 定义网络通讯类工具类的父类，定义了一些有用的接口
 */

open class SocketConnect(
		private val host: String,
		private val port: Int,
		private val ioException: (io: IOException) -> Unit = { it.printStackTrace() },
		private var timeout: Int? = null,
		private val socketTimeoutException: (e: SocketTimeoutException) -> Unit = { it.printStackTrace() },
		private val exception: (e: Exception) -> Unit = { it.printStackTrace() }) {

	private val socket: Socket = Socket()
	private var bufferedReader: BufferedReader? = null
	private var os: OutputStream? = null
	private var inputStream: InputStream? = null
	private val netThread = Executors.newSingleThreadExecutor()!!
	val address: String = "$host:$port"

	init {
		Log.d(TAG, "$address: init:")
		execute {
			if (timeout != null) {
				socket.soTimeout = timeout!!
				socket.connect(InetSocketAddress(host, port), timeout!!)
			} else {
				socket.connect(
						InetSocketAddress(host, port)
				)
			}
			bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
			os = socket.getOutputStream()
			inputStream = socket.getInputStream()
		}
	}

	fun close() {
		Log.d(TAG, "$address: close:")
		execute {
			if (!socket.isClosed) {
				socket.close()
			}
		}
	}

	fun execute(func: () -> Unit) {
		Log.d(TAG, "$address: execute: ${func.hashCode()}")
		netThread.execute {
			try {
				func()
			} catch (e: SocketTimeoutException) {
				socketTimeoutException(e)
			} catch (io: IOException) {
				ioException(io)
			} catch (e: Exception) {
				exception(e)
			}
		}
	}

	fun run(func: () -> Unit) {
		Log.d(TAG, "$address: run:")
		execute(func)
		close()
	}

	fun send(message: String) {
		Log.d(TAG, "$address: send:")
		os?.write(message.toByteArray())
	}

	fun recv(maxsize: Int = 102000, maxReadTime: Long = defaultMaxReadTime, maxWaitTime: Long = 100): String? {
		Log.d(TAG, "$address: recv(maxsize: Int, maxReadTime: Long, maxWaitTime: Long):")
		return String(recvByteArray(maxsize, maxReadTime, maxWaitTime) ?: return null)
	}

	fun recvSingle(maxsize: Int, maxReadTime: Long = defaultMaxReadTime, maxWaitTime: Long = defaultMaxWaitTime): String? {
		Log.d(TAG, "$address: recvSingle(maxsize: Int, maxReadTime: Long, maxWaitTime: Long):")
		return String(recvByteArraySingle(maxsize, maxReadTime, maxWaitTime) ?: return null)
	}

	fun recvByteArray(
			maxsize: Int = defaultReadSize * 10,
			maxReadTime: Long = defaultMaxReadTime,
			maxWaitTime: Long = 100)
			: ByteArray? {
		Log.d(TAG, "$address: recvByteArray(maxsize: Int, maxReadTime: Long, maxWaitTime: Long):")
		var buffer = recvByteArraySingle(defaultReadSize, maxReadTime, defaultMaxWaitTime)
				?: return null
		var loopTime = 0
		while ((buffer.size + loopTime * defaultReadSize) < maxsize && buffer.size == defaultReadSize) {
			buffer += (recvByteArraySingle(defaultReadSize, maxReadTime, maxWaitTime)
					?: return buffer)
			loopTime++
		}
		return buffer
	}

	fun recvByteArraySingle(
			maxsize: Int,
			maxReadTime: Long = defaultMaxReadTime,
			maxWaitTime: Long = defaultMaxWaitTime)
			: ByteArray? {
		Log.d(TAG, "$address: recvByteArraySingle(maxsize: Int, maxReadTime: Long, maxWaitTime: Long):")
		if (socket.isClosed) {
			System.err.println("socket closed")
			return null
		}
		inputStream ?: return null
		val buffer = ByteArray(maxsize)
		var readSize = 0
		try {
			//等待数据到达
			val maxTimeOut = System.currentTimeMillis() + maxWaitTime
			while (inputStream?.available() ?: 0 == 0) {
				if (System.currentTimeMillis() > maxTimeOut) {
					System.err.println("socket out of time")
					return null
				} else {
					sleep(10)
				}
			}

			//读取数据
			while (readSize < maxsize) {
				val readLength = java.lang.Math.min(
						inputStream?.available() ?: 0, maxsize - readSize)
				if (readLength <= 0) {
					break
				}
				// can alternatively use bufferedReader, guarded by isReady():
				val readResult = inputStream?.read(buffer, readSize, readLength) ?: -1
				if (readResult == -1) break
				readSize += readResult
				val maxTimeMillis = System.currentTimeMillis() + maxReadTime
				while (inputStream?.available() == 0) {
					if (System.currentTimeMillis() > maxTimeMillis) {
						return buffer.copyOf(readSize)
					}
				}
			}
		} catch (e: StringIndexOutOfBoundsException) {
			e.printStackTrace()
			return null
		}
		return buffer.copyOf(readSize)
	}

	fun isClosed() = socket.isClosed

	fun isConnected() = socket.isConnected

	companion object {
		const val defaultReadSize: Int = 10240
		const val defaultMaxReadTime: Long = 10
		const val defaultMaxWaitTime: Long = 10 * 1000
		val TAG = getTAG(this::class.java)
	}
}