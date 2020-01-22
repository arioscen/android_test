package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var mExitTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mWebView: WebView = findViewById(R.id.webView)
        val webSettings = mWebView.getSettings()
        webSettings.setJavaScriptEnabled(true)
        webSettings.setDomStorageEnabled(true)
        webSettings.setLoadWithOverviewMode(true)
        webSettings.setUseWideViewPort(true)
        webSettings.setBuiltInZoomControls(true)
        webSettings.setDisplayZoomControls(false)
        webSettings.setSupportZoom(true)
        webSettings.setDefaultTextEncodingName("utf-8")

        // 避免打開系統瀏覽器
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        // 顯示警告訊息
        mWebView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                val builder =  AlertDialog.Builder(this@MainActivity)
                builder.setTitle("JsAlert")
                builder.setMessage(message)
                builder.setPositiveButton("Confirm") {dialog, which ->
                    result.confirm()
                }
                builder.setCancelable(false)
                builder.show()
                return true
            }
        }

//        webView.loadUrl("https://appadmin.starone.com.tw/Default")
//        webView.loadUrl("http://10.1.2.250:8099/")
//        webView.loadUrl("http://10.1.1.123/")
        val config = readConfigFile()
        mWebView.loadUrl(config?.default_url)

        // 返回上一頁
        mWebView.setOnKeyListener(
            View.OnKeyListener {v, keyCode, event ->
                val action = event.action
                val webView = v as WebView
                if (KeyEvent.ACTION_DOWN == action && KeyEvent.KEYCODE_BACK == keyCode) {
                    if (webView.canGoBack()) {
                        webView.goBack()
                        return@OnKeyListener true
                    }
                }
                false
        })
    }

    // 聆聽設備旋轉事件
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
        }
    }

    // 設定返回退出
    override fun onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再點選一次「返回」退出應用", Toast.LENGTH_SHORT).show()
            mExitTime = System.currentTimeMillis()
        } else {
            super.onBackPressed()
        }
    }

    private var messageReceiver = object: BroadcastReceiver(){

        override fun onReceive(p0: Context?, p1: Intent?) {
            val simpleAlert = AlertDialog.Builder(this@MainActivity).create()
            simpleAlert.setTitle(p1?.getStringExtra("title"))
            simpleAlert.setMessage(p1?.getStringExtra("message"))
            simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialogInterface, i ->
                Toast.makeText(applicationContext, "You clicked on OK", Toast.LENGTH_SHORT).show()
            }
            simpleAlert.show()
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, IntentFilter("MyMessage"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    fun readConfigFile(): Config? {
        var config: Config? = null
        try {
            val inputStream: InputStream = assets.open("config.json")
            val json = inputStream.bufferedReader().use{it.readText()}
            config = Gson().fromJson(json, Config::class.java)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return config
    }

    class Config {
        var default_url = ""
    }
}
