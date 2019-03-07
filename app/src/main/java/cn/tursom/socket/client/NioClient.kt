package cn.tursom.socket.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

class NioClient(
	host: String,
	port: Int,
	bufferSize: Int = 4096,
	onWriteComplete: NioClient.(result: Int, buffer: ByteBuffer) -> Unit = { _, _ -> },
	onReadComplete: (NioClient.(result: Int, buffer: ByteBuffer) -> Unit)? = null
) : Closeable {
	val address = "$host:$port"
	private val channel = AsynchronousSocketChannel.open()!!
	private var writeHandler = Handler(onWriteComplete)
	private var readHandler = run {
		Handler(onReadComplete ?: return@run null)
	}
	private val buffer = ByteBuffer.allocate(bufferSize)
	
	init {
		channel.connect(InetSocketAddress(host, port)).get()
	}
	
	constructor(
		host: String,
		port: Int,
		bufferSize: Int = 4096,
		onWriteComplete: NioClient.(result: Int, buffer: ByteBuffer) -> Unit = { _, _ -> },
		onReadComplete: NioClient.(message: ByteArray) -> Unit
	) : this(host, port, bufferSize, onWriteComplete, { _, buffer ->
		buffer.flip()
		val limits = buffer.limit()
		val bytes = ByteArray(limits)
		buffer.get(bytes, 0, limits)
		buffer.clear()
		onReadComplete(bytes)
	})
	
	fun execute(func: NioClient.() -> Unit) = GlobalScope.launch(Dispatchers.IO) { func() }
	
	fun readHandler(onReadComplete: NioClient.(result: Int, buffer: ByteBuffer) -> Unit) {
		readHandler = Handler(onReadComplete)
	}
	
	fun writeHandler(onWriteComplete: NioClient.(result: Int, buffer: ByteBuffer) -> Unit) {
		writeHandler = Handler(onWriteComplete)
	}
	
	fun write(buffer: ByteBuffer) {
		channel.write(buffer, buffer, writeHandler)
	}
	
	fun write(data: ByteArray) {
		val buffer = ByteBuffer.allocate(data.size)
		buffer.put(data)
		buffer.flip()
		channel.write(buffer, buffer, writeHandler)
	}
	
	fun write(buffer: ByteBuffer, onWriteComplete: NioClient.(result: Int, buffer: ByteBuffer) -> Unit) {
		channel.write(buffer, buffer, Handler(onWriteComplete))
	}
	
	fun write(data: ByteArray, onWriteComplete: NioClient.(result: Int, buffer: ByteBuffer) -> Unit) {
		val buffer = ByteBuffer.allocate(data.size)
		buffer.put(data)
		buffer.flip()
		channel.write(buffer, buffer, Handler(onWriteComplete))
	}
	
	fun read() {
		channel.read(buffer, buffer, readHandler!!)
	}
	
	fun read(onReadComplete: NioClient.(result: Int, buffer: ByteBuffer) -> Unit) {
		channel.read(buffer, buffer, Handler(onReadComplete))
	}
	
	fun read(onReadComplete: (message: ByteArray) -> Unit) {
		channel.read(buffer, buffer, Handler { _, buffer ->
			buffer.flip()
			val limits = buffer.limit()
			val bytes = ByteArray(limits)
			buffer.get(bytes, 0, limits)
			buffer.clear()
			onReadComplete(bytes)
		})
	}
	
	override fun close() {
		try {
			channel.close()
		} catch (e: Exception) {
		}
	}
	
	private inner class Handler<T>(
		val handler: NioClient.(result: Int, buffer: T) -> Unit
	) : CompletionHandler<Int, T> {
		override fun completed(result: Int?, buffer: T) {
			handler(result!!, buffer)
		}
		
		override fun failed(e: Throwable, buffer: T) {
			e.printStackTrace()
		}
	}
}