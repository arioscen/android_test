package com.example.myapplication

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import android.util.Log
import android.view.Gravity
import android.webkit.*
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.android_web)
        val webSettings = webView.getSettings()
        webSettings.setJavaScriptEnabled(true)
        webSettings.setDomStorageEnabled(true)
        webSettings.setLoadWithOverviewMode(true)
        webSettings.setUseWideViewPort(true)
        webSettings.setBuiltInZoomControls(true)
        webSettings.setDisplayZoomControls(false)
        webSettings.setSupportZoom(true)
        webSettings.setDefaultTextEncodingName("utf-8")

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

        webView.addJavascriptInterface(JsObject(),"android")

//        然后加载JS代码
        webView.loadUrl("file:///android_asset/index.html")
        // 调用JS无参方法
        // get reference to button
        val btn_click_me = findViewById(R.id.android_btn) as Button
        // set on-click listener
        btn_click_me.setOnClickListener {
            webView.evaluateJavascript("javascript:clickJS()"
            ) { value -> Toast.makeText(this@MainActivity, value, Toast.LENGTH_LONG).show() }
        }
    }

    inner class JsObject {
        @JavascriptInterface
        fun jsAndroid(msg : String){
            //点击html的Button调用Android的Toast代码
            //我这里让Toast居中显示了
            val makeText = Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG)
            makeText.setGravity(Gravity.CENTER,0,0)
            makeText.show()
        }
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
}
