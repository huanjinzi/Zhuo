package test.hjz.zhuo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;

import test.hjz.zhuo.utils.UrlLoader;

public class WebActivity extends AppCompatActivity {

    private static final String TAG ="WebActivity";
    private WebView mWebView;
    private WebViewClient mWebViewClient;

    private Context mContext;
    private Handler mHandler;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);


        //https://blog.csdn.net/carson_ho/article/details/71402764
        mContext = getApplicationContext();
        mHandler = new UIHandler();
        mWebView = findViewById(R.id.webview);

        new Thread(() -> {
            content = UrlLoader.load("www.sogou.com");
            Log.d(TAG, content);
            mHandler.sendEmptyMessage(1);
        }).start();

        mWebViewClient = new MyWebViewClient();
        mWebView.setWebViewClient(mWebViewClient);

    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mWebView.loadData(content, "text/html", "utf-8");
        }
    }


    class MyWebViewClient extends WebViewClient {
        @SuppressLint("ResourceType")
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            //replace img
            if (request.getUrl().toString().contains(".png")) {
                Log.d(TAG, request.getUrl().toString() + " (be replaced!)");

                InputStream is = null;
                is = mContext.getResources().openRawResource(R.drawable.ic_launcher);

                WebResourceResponse response = new WebResourceResponse("image/png", "utf-8", is);
                return response;
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

}
