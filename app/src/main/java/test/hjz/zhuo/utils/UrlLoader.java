package test.hjz.zhuo.utils;

import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;

public class UrlLoader {

    private static final String TAG = "UrlLoader";
    // need net work permission

    //
    //MessageDigest
    public static String load(String url) {
        HttpURLConnection connection = null;
        URL relUrl = null;
        InputStreamReader reader = null;
        StringBuffer sb = null;
        try {
            relUrl = new URL(URLUtil.guessUrl(url));
            connection = (HttpURLConnection) relUrl.openConnection();

            connection.connect();

            Log.d(TAG, "ResponseCode = " + connection.getResponseCode() + ",getInstanceFollowRedirects=" + connection.getInstanceFollowRedirects() + ",url=" + relUrl);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                //

            }
            reader = new InputStreamReader(connection.getInputStream());

            if (reader == null) {
                return null;
            }
            BufferedReader bfr = new BufferedReader(reader);

            sb = new StringBuffer();
            String line = null;
            while ((line = bfr.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
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
        return sb == null ? null : sb.toString();
    }

    public static String getMimeType(String url) {

        HttpURLConnection connection = null;
        URL relUrl = null;
        InputStream in = null;

        try {
            relUrl = new URL(URLUtil.guessUrl(url));
            connection = (HttpURLConnection) relUrl.openConnection();

            in = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            // guessContentTypeFromStream,supports marks.(buffer input stream can works.)
            String mimeType = HttpURLConnection.guessContentTypeFromStream(bis);

            return mimeType;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static InputStream loadImage(String url){
        HttpURLConnection connection = null;
        URL relUrl = null;
        InputStream in = null;

        try {
            relUrl = new URL(URLUtil.guessUrl(url));
            connection = (HttpURLConnection) relUrl.openConnection();
            in = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }
}
