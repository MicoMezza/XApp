package com.example.xtv;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout fullscreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private MyWebChromeClient webChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        fullscreenContainer = findViewById(R.id.fullscreenContainer);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String script = "javascript:(function() {" +
                    "  document.addEventListener('play', function(e) {" +
                    "    if (e.target.tagName === 'VIDEO') {" +
                    "      if (e.target.requestFullscreen) {" +
                    "        e.target.requestFullscreen();" +
                    "      } else if (e.target.webkitRequestFullscreen) {" +
                    "        e.target.webkitRequestFullscreen();" +
                    "      }" +
                    "    }" +
                    "  }, true);" +
                    "})();";
                view.evaluateJavascript(script, null);
            }
        });

        webChromeClient = new MyWebChromeClient();
        webView.setWebChromeClient(webChromeClient);

        webView.loadUrl("https://x.com");
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            customView = view;
            customViewCallback = callback;
            
            webView.setVisibility(View.GONE);
            fullscreenContainer.addView(customView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            fullscreenContainer.setVisibility(View.VISIBLE);
        }

        @Override
        public void onHideCustomView() {
            if (customView == null) {
                return;
            }
            
            fullscreenContainer.setVisibility(View.GONE);
            fullscreenContainer.removeView(customView);
            customView = null;
            webView.setVisibility(View.VISIBLE);
            
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Se il video è a schermo intero, gestiamo i comandi del telecomando
        if (customView != null) {
            String jsCommand = null;
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                jsCommand = "var v = document.querySelector('video'); if(v){ v.paused ? v.play() : v.pause(); }";
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                jsCommand = "var v = document.querySelector('video'); if(v){ v.currentTime -= 10; }";
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                jsCommand = "var v.querySelector('video'); if(v){ v.currentTime += 10; }";
            }

            if (jsCommand != null) {
                webView.evaluateJavascript(jsCommand, null);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            webChromeClient.onHideCustomView();
        } else if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
