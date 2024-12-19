package com.example.breeze

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
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
        val pg_bar=findViewById<ProgressBar>(R.id.Pg_bar)
        pg_bar.visibility= ProgressBar.VISIBLE
        val webView = findViewById<WebView>(R.id.web)
        if (url != null) {

            webView.webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    pg_bar.visibility = ProgressBar.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    pg_bar.visibility = ProgressBar.GONE
                }
            }

            webView.settings.javaScriptEnabled = true // Enable JavaScript if required

            webView.loadUrl(url)

            //pg_bar.visibility= ProgressBar.INVISIBLE
        }
        //webView.loadUrl(url.toString())


    }
}


