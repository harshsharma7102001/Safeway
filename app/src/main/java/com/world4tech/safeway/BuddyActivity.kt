package com.world4tech.safeway

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.world4tech.safeway.databinding.ActivityBuddyBinding

class BuddyActivity : AppCompatActivity() {
    private lateinit var binding:ActivityBuddyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityBuddyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webView.webViewClient = WebViewClient()
        binding.webView.loadUrl("http://surakshitpath.000webhostapp.com/")
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.setSupportZoom(true)
    }
    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else
            super.onBackPressed()
    }

    // Overriding WebViewClient functions
    inner class WebViewClient : android.webkit.WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return false
        }
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.progressBar.visibility = View.GONE
        }
    }
}