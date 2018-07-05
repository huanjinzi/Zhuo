package test.hjz.zhuo.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ImageCache {

    public static String getFileName(String url) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] in = url.getBytes();

        messageDigest.update(in);
        byte[] ret = messageDigest.digest();
        StringBuffer sb = new StringBuffer();
        for (byte i : ret) {
            sb.append(Byte.toString(i));
        }
        return sb.toString();
    }

    public void writeToFile(){

        File cache_file = null;
        // write to file.
        OutputStream os = null;
        InputStream is = null;
        try {
            os = new FileOutputStream(cache_file);
            int len = 0;
            int bufferSize;
            byte[] buffer = new byte[1024];

            while ((bufferSize = is.read(buffer)) != -1) {
                os.write(buffer, len, bufferSize);
                len += bufferSize;
                //is = mContext.getResources().openRawResource(R.drawable.ic_launcher);
            }
        } catch (IOException e) {

        }
        Log.d("TAG", "cache write to:" + cache_file);
    }
}
