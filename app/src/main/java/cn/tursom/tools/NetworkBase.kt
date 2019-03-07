package cn.tursom.tools

import android.os.Handler
import android.os.Message

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.Executors

/**
 * Created by tursom on 2017/8/22.
 *
 *
 * class NetworkBase
 * 定义网络通讯类工具类的父类，定义了一些有用的接口
 */

fun getNewNetThread() = Executors.newSingleThreadExecutor()!!

open class NetworkBase internal constructor(
    // 定义向父线程发送消息的Handler对象
    private val superHandler: Handler? = null,
    private val host: String,
    private val port: Int,
    protected var timeout: Int? = null
) : Thread() {

    protected val socket = Socket()
    lateinit var bufferedReader: BufferedReader
    lateinit var os: OutputStream

    init {
        this.start()
    }

    final override fun run() {
        pre()

        try {
            if (timeout != null) {
                socket.soTimeout = timeout!!
                socket.connect(InetSocketAddress(host, port), timeout!!)
            } else {
                socket.connect(InetSocketAddress(host, port))
            }
            bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            os = socket.getOutputStream()

            main()

            if (!socket.isConnected) {
                socket.close()
            }
        } catch (e: SocketTimeoutException) {
            superHandler?.sendEmptyMessage(HandlerFunction.Timeout.ordinal)
        } catch (io: IOException) {
            io.printStackTrace()
            superHandler?.sendEmptyMessage(HandlerFunction.CannotConnectToServer.ordinal)
        }

        end()
    }

    protected open fun pre() {}

    protected open fun main() {}

    protected open fun end() {}

    protected fun sendMessage(what: Int) {
        superHandler ?: return
        superHandler.sendEmptyMessage(what)

    }

    protected fun sendOtherMessage(what: Int, obj: String) {
        superHandler ?: return
        val msg = Message()
        HandlerFunction.values().size
        msg.what = HandlerFunctionSize + what
        msg.obj = obj
        superHandler.sendMessage(msg)

    }

    protected fun sendOtherMessage(what: Int) {
        superHandler ?: return
        val msg = Message()
        HandlerFunction.values().size
        msg.what = HandlerFunctionSize + what
        superHandler.sendMessage(msg)

    }

    enum class HandlerFunction {
        CannotConnectToServer,
        Timeout,
        Print,
        Clear,
        ServerError,
        RegisterSuccess,
        RequestError,
        Other
    }

    companion object {
        val HandlerFunctionSize = HandlerFunction.values().size
    }

}
