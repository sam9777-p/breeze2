package com.example.breeze

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Webview : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val url=intent.getStringExtra("url")
        val webView = findViewById<WebView>(R.id.web)
        if (url != null) {
            webView.webViewClient = WebViewClient() // Prevent opening in an external browser
            webView.settings.javaScriptEnabled = true // Enable JavaScript if required
            webView.loadUrl(url)
        }
        //webView.loadUrl(url.toString())


    }
}

