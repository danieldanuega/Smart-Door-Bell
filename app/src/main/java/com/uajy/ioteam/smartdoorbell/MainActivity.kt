package com.uajy.ioteam.smartdoorbell

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.uajy.ioteam.smartdoorbell.R.id.output
import okhttp3.*
import okio.ByteString
import com.uajy.ioteam.smartdoorbell.R.id.output
import com.uajy.ioteam.smartdoorbell.R.id.output


class MainActivity : AppCompatActivity() {

    var output:TextView? = null
    private var button:Button? = null

    private inner class EchoWebSocketListener: WebSocketListener() {

        val NORMAL_CLOSURE_STATUS = 1000

        override fun onOpen(webSocket: WebSocket?, response: Response?) {
            super.onOpen(webSocket, response)

            webSocket?.send("{\n" +
                    "  \"type\": \"register\",\n" +
                    "  \"content\": {\n" +
                    "    \"type\": \"client\"\n" +
                    "  }\n" +
                    "}")
        }

        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            super.onFailure(webSocket, t, response)
            output("Error : ${t?.message}")
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            super.onClosing(webSocket, code, reason)
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            output("Closing : $code  / $reason")
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            super.onMessage(webSocket, text)
            output("Receiving : $text")
        }

        override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
            super.onMessage(webSocket, bytes)
            output("Receiving bytes : ${bytes?.hex()}")
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            super.onClosed(webSocket, code, reason)
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            output("Closing : $code / $reason")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button) as Button
        output = findViewById(R.id.output) as TextView
        val client = OkHttpClient()

        button?.setOnClickListener {

            val request:Request = Request.Builder().url("ws://10.0.2.2:8181").build()
            val listener:EchoWebSocketListener = EchoWebSocketListener()
            val ws:WebSocket = client.newWebSocket(request, listener)

            client.dispatcher().executorService().shutdown()
        }
    }

    private fun output(txt: String) {
        runOnUiThread { output?.setText(output?.getText().toString() + "\n\n" + txt) }
    }
}
