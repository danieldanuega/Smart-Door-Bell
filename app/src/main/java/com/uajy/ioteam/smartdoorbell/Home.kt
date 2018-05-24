package com.uajy.ioteam.smartdoorbell

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.*
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject


class Home : AppCompatActivity() {

    //private var output:TextView? = null
    private var btnLogout:Button? = null
    private var btnSound:Button? = null
    private var btnCopyId:Button? = null
    private var id:Int? = null
    var session:SharedPreferences? = null
    var txtId:TextView? = null
    var txtInfo:TextView? = null

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
            //output("Error : ${t?.message}")
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            super.onClosing(webSocket, code, reason)
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            //output("Closing : $code  / $reason")
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            super.onMessage(webSocket, text)
            output("$text")
        }

        override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
            super.onMessage(webSocket, bytes)
            //output("Receiving bytes : ${bytes?.hex()}")
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            super.onClosed(webSocket, code, reason)
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            //output("Closing : $code / $reason")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Session
        session = this.getSharedPreferences("Session", Context.MODE_PRIVATE)
        /*val editor = session!!.edit()
        editor.putString("id",temp)
        editor.apply()*/

        //Connect UI with code
        btnLogout = findViewById(R.id.btnLogout) as Button
        btnSound = findViewById(R.id.btnSound) as Button
        btnCopyId = findViewById(R.id.btnCopyId) as Button
        txtId = findViewById(R.id.txtId) as TextView
        txtInfo = findViewById(R.id.txtInfo) as TextView

        //Open WebSocket
        val client = OkHttpClient()
        val request:Request = Request.Builder().url(session!!.getString("ip", "-")).build()
        val listener = EchoWebSocketListener()
        val ws:WebSocket = client.newWebSocket(request, listener)


        btnSound!!.setOnClickListener {

            if(id == null)
                copyId()

            ws.send("{\n" +
                    "  \"type\": \"soundOn\",\n" +
                    "  \"content\": {\n" +
                    "    \"id\": $id\n" +
                    "  }\n" +
                    "}")
        }

        btnLogout!!.setOnClickListener {

            val editor = session!!.edit()
            editor.clear()
            editor.apply()

            //To Shutdown client and ws
            ws.close(0,"Manually Closed")
            client.dispatcher().executorService().shutdown()

            val intent = Intent(this@Home, WelcomeScreen::class.java)
            startActivity(intent)
        }
    }

    //Copy to Info
    private fun copyId() {

        val req = JSONObject(txtInfo!!.text.toString())
        val obj = req.getJSONObject("content")
        id = obj.getInt("id")
    }

    private fun output(txt: String) {
        runOnUiThread { txtInfo?.setText(txt) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}
