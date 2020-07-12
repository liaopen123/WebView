package com.example.commonweb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


public abstract class BaseH5Activity extends Activity {
    private static final String TAG = "BaseH5Activity";
    private int percent = 0;
    Context mContext;
    private OnPageFinishListener onPageFinishListener;
    public String currentUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext =this;

    }


    @SuppressLint("JavascriptInterface")
    public void initWebView(final WebView webView, final ProgressBar progressBar, final GetTitleListner getTitleListner) {
        if (webView != null) {
            webView.setDownloadListener(new MyWebViewDownLoadListener());//下载设置

//

            WebSettings ws = webView.getSettings();


            ws.setBuiltInZoomControls(true);
            ws.setSupportZoom(true);
            //与js交互必须设置
            ws.setJavaScriptEnabled(true);//js可用
            ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
            ws.setDomStorageEnabled(true);
            //设置加载进来的页面自适应手机屏幕
            ws.setUseWideViewPort(true);
            ws.setLoadWithOverviewMode(true);
            webView.addJavascriptInterface(BaseH5Activity.this, "android");

            // 网页内容的宽度是否可大于WebView控件的宽度
            ws.setLoadWithOverviewMode(false);
            // 保存表单数据
            ws.setSaveFormData(true);
            // 是否应该支持使用其屏幕缩放控件和手势缩放
            ws.setSupportZoom(true);
            ws.setBuiltInZoomControls(true);
            ws.setDisplayZoomControls(false);
            // setDefaultZoom  api19被弃用
            // 设置此属性，可任意比例缩放。
            ws.setUseWideViewPort(true);
//            不缩放
            webView.setInitialScale(100);
//             页面加载好以后，再放开图片
            ws.setBlockNetworkImage(false);

//            排版适应屏幕
            ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
//            webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            /** 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)*/
            ws.setTextZoom(100);//解决网页图片伸展不到屏幕边缘的bug


            webView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    checkCanGoBack(webView);
                    if (newProgress == 100) {
                        progressBar.setVisibility(View.GONE);
                    } else {
                        if (View.GONE == progressBar.getVisibility()) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        progressBar.setProgress(newProgress);
                    }
                    super.onProgressChanged(view, newProgress);
                }
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    getTitleListner.getTitle(title);
                }


            });

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();// 接受所有网站的证书
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    Log.e(TAG, "onReceivedError: ");

                    if (request.isForMainFrame()) {
                        onPageError();
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    try{
                        if(!url.startsWith("tel:")) {
                            CookieManager cookieManager = CookieManager.getInstance();
                            String CookieStr = cookieManager.getCookie(url);
                            Log.e(TAG, "$CookieStr:" + CookieStr);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if (onPageFinishListener != null) {
                        onPageFinishListener.onPageFinish();
                    }
                    super.onPageFinished(view, url);
                }
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i(TAG, "shouldOverrideUrlLoading:url:" + url);
                    currentUrl  = url;

                    if (url.startsWith("tel:")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);

                }
            });


            try {
                WebSettings webseting = webView.getSettings();
                webseting.setDomStorageEnabled(true);
                webseting.setAppCacheMaxSize(1024 * 1024 * 8);//设置缓冲大小，我设的是8M
                String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
                webseting.setAppCachePath(appCacheDir);
                webseting.setAllowFileAccess(true);
                webseting.setAppCacheEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void onPageError();

    private void checkCanGoBack(WebView webView) {
//        if (webView.canGoBack()) {
//            mTitleLayoutView.setCloseVisibility(View.VISIBLE);
//            mTitleLayoutView.setCloseClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    finish();
//                }
//            });
//        } else {
//            mTitleLayoutView.setCloseVisibility(View.GONE);
//        }
    }

    public void webDestroy(WebView wvWeb) {
        if (wvWeb != null) {
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) icon_return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = wvWeb.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(wvWeb);
            }

            wvWeb.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            wvWeb.getSettings().setJavaScriptEnabled(false);
            wvWeb.clearHistory();
            wvWeb.clearView();
            wvWeb.removeAllViews();

            try {
                wvWeb.destroy();
            } catch (Throwable ex) {

            }
        }
    }

    public interface GetTitleListner {
        void getTitle(String title);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

    }




    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
//            Log.i("tag", "url="+url);
//            Log.i("tag", "userAgent="+userAgent);
//            Log.i("tag", "contentDisposition="+contentDisposition);
//            Log.i("tag", "mimetype="+mimetype);
//            Log.i("tag", "contentLength="+contentLength);
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
        }
    }

    public    void removeCookie(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
    }

    public void setonPageFinishListener(OnPageFinishListener onPageFinishListener){
        this.onPageFinishListener = onPageFinishListener;
    }

    public interface  OnPageFinishListener{
        void  onPageFinish();
    }





}
