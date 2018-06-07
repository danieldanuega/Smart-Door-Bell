package com.uajy.ioteam.smartdoorbell

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.*
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import android.graphics.Color
import android.os.Build
import android.os.Message
import android.support.v4.app.NotificationManagerCompat
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*


class Home : AppCompatActivity() {

    val NORMAL_CLOSURE_STATUS = 1000

    //private var output:TextView? = null
    private var btnLogout:Button? = null
    private var btnSound:ImageView? = null
    private var btnMessage:Button? = null
    //private var btnCopyId:Button? = null
    private var id:Int? = null
    var session:SharedPreferences? = null
    //var txtId:TextView? = null
    var txtInfo:TextView? = null
    var txtMessage:TextView? = null
    //var progBar:ProgressBar? = null

    //NOTIFICATION
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder : Notification.Builder
    private val channelId = "com.uajy.ioteam.smartdoorbell"
    private val description = "Test Notification"



    private inner class EchoWebSocketListener: WebSocketListener() {

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

            val req = JSONObject(text)
            val type:String = req.getString("type")

            if(type.equals("buttonPressed")) {
                sendNotification(type)
            }

            //progBar?.setVisibility(VISIBLE)
            output("$text")
            //progBar?.setVisibility(INVISIBLE)
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

        //Notification
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Session
        session = this.getSharedPreferences("Session", Context.MODE_PRIVATE)
        /*val editor = session!!.edit()
        editor.putString("id",temp)
        editor.apply()*/

        //Connect UI with code
        btnLogout = findViewById(R.id.btnLogout) as Button
        btnSound = findViewById(R.id.btnSound) as ImageView
        btnMessage = findViewById(R.id.btnMessage) as Button
        //btnCopyId = findViewById(R.id.btnCopyId) as Button
        txtMessage = findViewById(R.id.txtMessage) as TextView
        txtInfo = findViewById(R.id.txtInfo) as TextView
        //progBar = findViewById(R.id.progressBar) as ProgressBar


        //Open WebSocket
        val client = OkHttpClient()
        val request:Request = Request.Builder().url(session!!.getString("ip", "-")).build()
        val listener = EchoWebSocketListener()
        val ws:WebSocket = client.newWebSocket(request, listener)


        btnSound!!.setOnClickListener {

            if(id == null)
                copyId()

            Toast.makeText(this, "Ding Dong!", Toast.LENGTH_SHORT).show()
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
            ws.close(NORMAL_CLOSURE_STATUS,"Manually Closed")
            client.dispatcher().executorService().shutdown()

            val intent = Intent(this@Home, WelcomeScreen::class.java)
            startActivity(intent)
        }

        btnMessage!!.setOnClickListener {

            if(id == null)
                copyId()

            ws.send("{\n" +
                    "  \"type\": \"setText\",\n" +
                    "  \"content\": {\n" +
                    "    \"id\": $id,\n" +
                    "    \"text\": \"${txtMessage?.text.toString()}\"\n" +
                    "  }\n" +
                    "}")
        }
    }


    private fun sendNotification(message: String?) {

        val intent = Intent(this, Home::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId,description,NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLACK
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this,channelId)
                    .setContentTitle("Home IoT")
                    .setContentText("$message")
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent)

        } else {

            builder = Notification.Builder(this)
                    .setContentTitle("Home IoT")
                    .setContentText("$message")
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent)
        }

        notificationManager.notify(13,builder.build())
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
