package com.uajy.ioteam.smartdoorbell


import android.os.Handler
import android.os.Message

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class ServerConnection(private val mServerUrl: String? = null) {

    private var mWebSocket: WebSocket? = null
    private val mClient: OkHttpClient
    private var mMessageHandler: Handler? = null
    private var mStatusHandler: Handler? = null
    private var mListener: ServerListener? = null

    enum class ConnectionStatus {
        DISCONNECTED,
        CONNECTED
    }

    interface ServerListener {
        fun onNewMessage(message: String)
        fun onStatusChange(status: ConnectionStatus)
    }


    private inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket?, response: Response?) {
            val m = mStatusHandler!!.obtainMessage(0, ConnectionStatus.CONNECTED)
            mStatusHandler!!.sendMessage(m)
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            val m = mMessageHandler!!.obtainMessage(0, text)
            mMessageHandler!!.sendMessage(m)
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            val m = mStatusHandler!!.obtainMessage(0, ConnectionStatus.DISCONNECTED)
            mStatusHandler!!.sendMessage(m)
        }

        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            disconnect()
        }
    }

    init {
        mClient = OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
    }

    fun connect(listener: ServerListener) {
        val request = Request.Builder()
                .url(mServerUrl)
                .build()
        mWebSocket = mClient.newWebSocket(request, SocketListener())
        mListener = listener
        mMessageHandler = Handler { msg ->
            mListener!!.onNewMessage(msg.obj as String)
            true
        }
        mStatusHandler = Handler { msg ->
            mListener!!.onStatusChange(msg.obj as ConnectionStatus)
            true
        }
    }

    fun disconnect() {
        mWebSocket!!.cancel()
        mListener = null
        mMessageHandler!!.removeCallbacksAndMessages(null)
        mStatusHandler!!.removeCallbacksAndMessages(null)
    }

    fun sendMessage(message: String) {
        mWebSocket!!.send(message)
    }
}