package cn.tursom.android.client

import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit

class NioClient(
	host: String,
	port: Int,
	bufferSize: Int = 4096,
	private val timeout: Long = 60 * 1000,
	onWriteComplete: (result: Int, buffer: ByteBuffer) -> Unit = { _, _ -> },
	onReadComplete: ((result: Int, buffer: ByteBuffer) -> Unit)? = null
) : Closeable {
	val address = "$host:$port"
	private val channel = AsynchronousSocketChannel.open()!!
	private val writeHandler = Handler(onWriteComplete)
	private val readHandler by lazy {
		Handler(onReadComplete ?: return@lazy null)
	}
	private val buffer = ByteBuffer.allocate(bufferSize)
	
	init {
		channel.connect(InetSocketAddress(host, port)).get()
	}
	
	val isOpen
		get() = channel.isOpen
	
	private fun <A> write(
		src: ByteBuffer,
		timeout: Long,
		attachment: A,
		handler: CompletionHandler<Int, in A>
	) {
		channel.write(src, timeout, TimeUnit.MICROSECONDS, attachment, handler)
	}
	
	fun write(buffer: ByteBuffer) {
		write(buffer, timeout, buffer, writeHandler)
	}
	
	fun write(data: ByteArray) {
		val buffer = ByteBuffer.allocate(data.size)
		buffer.put(data)
		buffer.flip()
		write(buffer, timeout, buffer, writeHandler)
	}
	
	fun write(buffer: ByteBuffer, onWriteComplete: (result: Int, buffer: ByteBuffer) -> Unit) {
		write(buffer, timeout, buffer, Handler(onWriteComplete))
	}
	
	fun write(data: ByteArray, onWriteComplete: (result: Int, buffer: ByteBuffer) -> Unit) {
		val buffer = ByteBuffer.allocate(data.size)
		buffer.put(data)
		buffer.flip()
		write(buffer, timeout, buffer, Handler(onWriteComplete))
	}
	
	
	private fun <A> read(
		dst: ByteBuffer,
		timeout: Long,
		attachment: A,
		handler: CompletionHandler<Int, in A>
	) {
		channel.read(dst, timeout, TimeUnit.MICROSECONDS, attachment, handler)
	}
	
	fun read() {
		read(buffer, timeout, buffer, readHandler!!)
	}
	
	fun read(onReadComplete: (result: Int, buffer: ByteBuffer) -> Unit) {
		read(buffer, timeout, buffer, Handler(onReadComplete))
	}
	
	fun read(onReadComplete: (message: ByteArray) -> Unit) {
		read(buffer, timeout, buffer, Handler { _, buffer ->
			buffer.flip()
			val limits = buffer.limit()
			val bytes = ByteArray(limits)
			buffer.get(bytes, 0, limits)
			onReadComplete(bytes)
		})
	}
	
	override fun close() {
		try {
			channel.close()
		} catch (e: Exception) {
		}
	}
	
	private class Handler<T>(
		val handler: (result: Int, buffer: T) -> Unit
	) : CompletionHandler<Int, T> {
		override fun completed(result: Int?, buffer: T) {
			handler(result!!, buffer)
		}
		
		override fun failed(e: Throwable, buffer: T) {
			e.printStackTrace()
		}
	}
}