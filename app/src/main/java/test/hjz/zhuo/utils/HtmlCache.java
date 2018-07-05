package test.hjz.zhuo.utils;

import android.util.Log;

import java.util.HashMap;

public class HtmlCache {
    private static final String TAG = "HtmlCache";

    // other
    private static HashMap<String, String> map = new HashMap<>();

    public static String get(String url){
        return map.get(url);
    }

    public static void put(String url,String content){
        map.put(url, content);
        Log.d(TAG, "put: url=" + url + "\ncontent:" + content);
    }

    public static void clear(){
        map.clear();
    }
}
