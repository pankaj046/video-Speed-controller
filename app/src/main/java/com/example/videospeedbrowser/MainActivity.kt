package com.example.videospeedbrowser

import android.annotation.SuppressLint
import android.app.*
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videospeedbrowser.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var isPageError = false

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isPageError = false
            }
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean { return false }
            override fun onPageFinished(view: WebView, url: String) { super.onPageFinished(view, url)
                if (isPageError){
                    binding.webView.visibility = View.GONE
                }}

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                isPageError = true
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                view?.let {
                    it.evaluateJavascript("document.querySelector('video').playing;"
                    ) { value ->
                        Log.e("TAG", "doUpdateVisitedHistory: $value")
                        if (value?.equals("{}", ignoreCase = true) == true) {
                            binding.button.visibility = View.VISIBLE
                        } else {
                            binding.button.visibility = View.GONE
                        }
                    }
                }
            }

        }

        binding.webView.webChromeClient = MyChrome(this)
        val webSettings: WebSettings = binding.webView.settings
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true
        webSettings.pluginState = WebSettings.PluginState.ON_DEMAND
        binding.webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        binding.webView.isScrollbarFadingEnabled = true
        webSettings.pluginState = android.webkit.WebSettings.PluginState.ON
        webSettings.loadsImagesAutomatically = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        binding.webView.loadUrl("https://m.youtube.com")
        val list = arrayListOf("0.10", "0.15", "0.20", "0.25", "0.30", "0.35","0.40","0.45","0.50","0.55","0.60","0.65", "0.70","0.75", "1.0", "1.05", "1.10", "1.15", "1.20", "1.25", "1.30", "1.35","1.40","1.45","1.50","1.55","1.60","1.65", "1.70","1.75", "2.0", "2.05", "2.10", "2.15", "2.20", "2.25", "2.30", "2.35","2.40","2.45","2.50","2.55","2.60","2.65", "2.70","2.75","3.00")

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@MainActivity,
            android.R.layout.simple_spinner_dropdown_item,
            list
        )

        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)
        val bottomSheetView: View = layoutInflater.inflate(R.layout.bottom_sheet_view, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetView.setBackgroundColor(ContextCompat.getColor(this, R.color.black))

        val sheetListView: ListView = bottomSheetDialog.findViewById(R.id.bottom)!!
        sheetListView.adapter = adapter
        sheetListView.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        sheetListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                val js = "document.querySelector('video').playbackRate = ${list[position]},void(0);"
                binding.webView.evaluateJavascript(js, null)
                bottomSheetDialog.dismiss()
            }
        binding.button.visibility = View.GONE
        binding.button.setOnClickListener {
            bottomSheetDialog.show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action === KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
        binding.webView.pauseTimers()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
        binding.webView.resumeTimers()
    }


    private class MyChrome(val context: Activity) : WebChromeClient() {

        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0
        override fun getDefaultVideoPoster(): Bitmap? {

            return if (mCustomView == null) {
                null
            } else{
//                val button = context.findViewById<MovableFloatingActionButton>(R.id.button)
//                button.visibility = View.VISIBLE
                BitmapFactory.decodeResource(context.resources, 2130837573)
            }
        }


        override fun getVideoLoadingProgressView(): View? {
            return super.getVideoLoadingProgressView()
        }

        override fun onHideCustomView() {
            (context.window.decorView as FrameLayout).removeView(mCustomView)
            mCustomView = null
            context.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
            context.requestedOrientation = mOriginalOrientation
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomViewCallback = null
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        override fun onShowCustomView(
            paramView: View?,
            paramCustomViewCallback: CustomViewCallback?
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility = context.window.decorView.systemUiVisibility
            mOriginalOrientation = context.requestedOrientation
            mCustomViewCallback = paramCustomViewCallback
            (context.window.decorView as FrameLayout).addView(
                mCustomView,
                FrameLayout.LayoutParams(-1, -1)
            )
            context.window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.webView.restoreState(savedInstanceState)
    }

}
