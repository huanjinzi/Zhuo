package test.hjz.zhuo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import test.hjz.zhuo.utils.HtmlCache;
import test.hjz.zhuo.utils.ImageCache;
import test.hjz.zhuo.utils.UrlLoader;

public class WebActivity extends AppCompatActivity {

    private static final String TAG = "WebActivity";
    private WebView mWebView;
    private WebViewClient mWebViewClient;

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;

    private Context mContext;
    private Handler mUIHandler;
    private String url;
    private String content;
    private MessageDigest mMessageDigest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        //
        url = getIntent().getStringExtra("url");
        mContext = getApplicationContext();
        mUIHandler = new UIHandler();
        mWebView = findViewById(R.id.webview);

        // init work thread.
        mWorkThread = new HandlerThread("WebTask", Process.THREAD_PRIORITY_BACKGROUND);
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());

        //web view
        mWebViewClient = new MyWebViewClient();
        mWebView.setWebViewClient(mWebViewClient);

        mWorkHandler.post(() -> {

            // content
            if ((content = HtmlCache.get(url)) == null) {
                content = UrlLoader.load(url);
                if (content != null) {
                    HtmlCache.put(url, content);
                } else {
                    content = "error!";
                }
            }

            // notify data load complete.
            Log.d(TAG, "mWorkHandler content loaded!" + content);
            mUIHandler.sendEmptyMessage(0);
        });

    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (content == null) {
                content = "UIHandler error!";
            }
            Log.d(TAG, "UIHandler receive:" + content);
            mWebView.loadData(content, "text/html", "utf-8");
        }
    }


    class MyWebViewClient extends WebViewClient {
        @SuppressLint("ResourceType")
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            //replace img
            // there is not in UI thread.
            String url = request.getUrl().toString();
            String mimeType = UrlLoader.getMimeType(url);
            if (mimeType != null) {
                Log.d(TAG, "mimeType = " + mimeType);
                if (mimeType.contains("image/")) {
                    Log.d(TAG, url + " (be replaced!)");

                    //todo deal picture.
                    String file_name = ImageCache.getFileName(url);
                    File cache_file = new File(mContext.getCacheDir().getPath() + "/img" + file_name);

                    // 1.
                    Log.d(TAG, "cache_file:" + cache_file);
                    InputStream is = null;
                    if (!cache_file.exists()) {
                        Log.d(TAG, "cache not exists,load from network.");

                        // use for test
                        //is = mContext.getResources().openRawResource(R.drawable.ic_launcher);
                        is = UrlLoader.loadImage(url);
                    }

                    // 2.write to cache file.
                    // is == null, no need to load image from network.
                    if (is != null) {
                        OutputStream os = null;
                        try {
                            os = new FileOutputStream(cache_file);
                            int bufferSize;
                            byte[] buffer = new byte[1024];

                            Log.d(TAG, "cache_file can write:" + cache_file.canWrite());
                            assert is != null;
                            while ((bufferSize = is.read(buffer)) != -1) {
                                Log.d(TAG, "writing......");
                                os.write(buffer, 0, bufferSize);
                            }
                            Log.d(TAG, "cache write to:size = " + cache_file.length() / 1024 + "KB,path=" + cache_file);

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                is.close();
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // 3.load cache from file.
                    InputStream input = null;
                    try {
                        input = new FileInputStream(cache_file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    cache_file.getTotalSpace();
                    Log.d(TAG, "load cache from local:" + cache_file);
                    WebResourceResponse response = new WebResourceResponse(mimeType, "utf-8",input);
                    return response;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

}
