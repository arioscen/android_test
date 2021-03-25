package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var mExitTime = 0L
    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 啟動時檢查版本
        checkVersion()

        mWebView = findViewById(R.id.webView)
        val webSettings = mWebView?.getSettings()
        webSettings!!.setJavaScriptEnabled(true)
        webSettings.setDomStorageEnabled(true)
        webSettings.setLoadWithOverviewMode(true)
        webSettings.setUseWideViewPort(true)
        webSettings.setBuiltInZoomControls(true)
        webSettings.setDisplayZoomControls(false)
        webSettings.setSupportZoom(true)
        webSettings.setDefaultTextEncodingName("utf-8")

        // 避免打開系統瀏覽器
        mWebView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        // 顯示警告訊息
        mWebView?.webChromeClient = object : WebChromeClient() {
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

        mWebView?.loadUrl("https://mobile.starone.com.tw/")

        // 返回上一頁
        mWebView?.setOnKeyListener(
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

        // Toolsbar
//        val toolbar = findViewById(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolbar)
//        getSupportActionBar()?.setDisplayShowTitleEnabled(false)
//        toolbar.setNavigationOnClickListener {
//            mWebView?.goBack()
//        }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.myHome -> {
            mWebView?.loadUrl("https://mobile.starone.com.tw/")
            true
        }
        R.id.myList -> {
            Toast.makeText(this, "List button click", Toast.LENGTH_SHORT).show()
            true
        }
        else -> super.onOptionsItemSelected(item)
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

    class NewVersion {
        var msg = ""
        var data = emptyArray<VersionData>()
    }

    class VersionData {
        var version = ""
    }

    fun forceUpdate() {
        val intentUrl = "https://play.google.com/store/apps/details?id=com.spotify.music"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(intentUrl))
        startActivity(intent)
    }

    fun checkVersion() {
        // 讀取目前的版本號
        val configFileStream = getAssets().open("config.json")
        val size = configFileStream.available()
        val buffer = ByteArray(size)
        configFileStream.read(buffer)
        val charset = Charsets.UTF_8
        val configJson = String(buffer, charset)
        val appVersion = Gson().fromJson(configJson, VersionData::class.java)
        val appVersionNumbers = appVersion.version.split(".").toTypedArray()

        // 取得最新的版本號
        val queue = Volley.newRequestQueue(this)
        val url = "http://172.31.28.31/api/app/latest-version?system=android"
        val stringRequest = object : StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val newVersion = Gson().fromJson(response, NewVersion::class.java)
                val newVersionNumbers = newVersion.data[0].version.split(".").toTypedArray()

                // 檢查目前版本跟最新版本是否有差異
                for (i in appVersionNumbers.indices) {
                    if (appVersionNumbers[i] < newVersionNumbers[i]) {
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("請更新至最新版 " + newVersion.data[0].version)
                        builder.setPositiveButton("確認") {dialog, which ->}
                        builder.setOnDismissListener {
                            // 提示更新後，強制更新
                            forceUpdate()
                        }
                        builder.show()
                        break
                    }
                }
            },
            Response.ErrorListener {
                // 檢查版本連結失敗，強制更新
                forceUpdate()
            }) {
            override fun getHeaders(): Map<String, String>? {
//                return emptyMap()
                return mapOf("api-app-id" to "crm_api", "api-app-secure" to "3ca107511912f3f7eb5bed764d00389d")
            }
        }
        queue.add(stringRequest)
    }
}
