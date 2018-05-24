package com.uajy.ioteam.smartdoorbell

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.content.SharedPreferences
import android.view.Window
import android.view.WindowManager
import android.widget.Button


class WelcomeScreen : Activity() {

    var session:SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.welcome_screen)

        session = getSharedPreferences("Session", Context.MODE_PRIVATE)

        val txtServer = findViewById(R.id.txtServer) as TextView
        val btnStart = findViewById(R.id.btnStart) as Button

        btnStart.setOnClickListener {

            val editor = session!!.edit()
            editor.clear()
            editor.putString("ip", txtServer.text.toString())
            editor.apply()

            startActivity(Intent(this.applicationContext, Home::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        val ip = session!!.getString("ip","-")

        if(!ip.equals("-")) {
            startActivity(Intent(this.applicationContext, Home::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}

