package com.example.myapplication

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import android.util.Log
import android.view.KeyEvent
import android.view.View

class MainActivity : AppCompatActivity() {
    private var mExitTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webView)
        val webSettings = webView.getSettings()
        webSettings.setJavaScriptEnabled(true)
        webSettings.setDomStorageEnabled(true)
        webSettings.setLoadWithOverviewMode(true)
        webSettings.setUseWideViewPort(true)
        webSettings.setBuiltInZoomControls(true)
        webSettings.setDisplayZoomControls(false)
        webSettings.setSupportZoom(true)
        webSettings.setDefaultTextEncodingName("utf-8")
        webView.setOnKeyListener(onKeyEvent)

        // 避免打開系統瀏覽器
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        })

        // 顯示警告訊息
        webView.setWebChromeClient(object : WebChromeClient() {
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
        })

//        webView.loadUrl("https://appadmin.starone.com.tw/Default")
//        webView.loadUrl("http://10.1.2.250:8099/")
//        webView.loadUrl("http://10.1.1.123/")
                webView.loadUrl("http://10.1.1.102/")
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

    // 設定返回上一頁
    private val onKeyEvent = View.OnKeyListener { v, keyCode, event ->
        val action = event.action
        val webView = v as WebView
        if (KeyEvent.ACTION_DOWN == action && KeyEvent.KEYCODE_BACK == keyCode) {
            if (webView.canGoBack()) {
                webView.goBack()
                return@OnKeyListener true
            }
        }
        false
    }
}
