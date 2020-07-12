package com.example.commonweb

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_web_view.*


class WebViewActivity : BaseH5Activity() {
    private var url: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web_view);
        initData()
    }

    fun initData() {
        url = "http://39.105.8.203/wangdademo/"

        initWebView(
            wv, myProgressBar
        ) { title ->
            this.title = title
        }
        wv.loadUrl(url)
    }



    override fun onBackPressed() {
        if (wv.canGoBack()) {
            wv.goBack()
        } else {
            finish()
        }
    }

    override fun onPageError() {

    }


    override fun onDestroy() {
        webDestroy(wv)
        super.onDestroy()
    }


}
