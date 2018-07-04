package test.hjz.zhuo.utils;

import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class UrlLoader {

    private static final String TAG = "UrlLoader";
    // need net work permission

    //
    //MessageDigest
    public static String load(String url){
        HttpURLConnection connection = null;
        URL relUrl = null;
        InputStreamReader reader = null;
        StringBuffer sb = null;
        try {
            relUrl = new URL(URLUtil.guessUrl(url));
            connection = (HttpURLConnection) relUrl.openConnection();
            Log.d(TAG, "ResponseCode = " + connection.getResponseCode());
            reader = new InputStreamReader(connection.getInputStream());

            if (reader == null) {
                return "error";
            }
            BufferedReader bfr = new BufferedReader(reader);

            sb = new StringBuffer();
            String line = null;
            while ((line = bfr.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException  e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
