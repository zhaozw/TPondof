package fish.pondof.tpondof.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;
import java.util.Map;

import fish.pondof.tpondof.App;
import fish.pondof.tpondof.R;
import fish.pondof.tpondof.api.ApiManager;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static fish.pondof.tpondof.BuildConfig.DEBUG;

/**
 * Created by Administrator on 2017/2/22.
 * @see <a href="http://www.cnblogs.com/ct2011/p/4001708.html" />
 * @see <a href="http://www.jianshu.com/p/d7777c828ee8" />
 */

public class NetworkUtil {
    private static final String TAG = "Network";
    public static String get (String url, boolean useCache) throws IOException {
        if (DEBUG) Log.i(TAG, "Get requested:" + url);
        if (DEBUG) Log.i(TAG, "Use cache:" + useCache);
        String cache = App.getCache().getAsString(url);
        if (useCache && cache != null && !cache.isEmpty()) {
            if (DEBUG) Log.i(TAG, "Get from cache");
            return cache;
        }
        if (DEBUG) Log.i(TAG, "Get from web");
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        String responseString = response.body().string();
        App.getCache().put(url, responseString);
        if (DEBUG) Log.i(TAG, "Writing to cache");
        return responseString;
    }
    public static String post (String url, Map<String, String> value) throws IOException {
        FormBody.Builder b = new FormBody.Builder();
        for (String s : value.keySet()) {
            b.add(s, value.get(s));
        }
        Request request = new Request.Builder().post(b.build())
                .url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        return response.body().string();
    }

    public static void loadImage (String url, final ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.mipmap.ic_avatar);
            return;
        }
        buildPicasso(imageView.getContext(), url).into(imageView);
    }
    public static RequestCreator buildPicasso (Context context, String url) {
        return Picasso.with(context)
                .load(url)
                .placeholder(R.mipmap.ic_avatar)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.mipmap.ic_avatar);
    }

    @Nullable
    public static Bitmap fromCache (Context context, String url) {
        RequestCreator creator = buildPicasso(context, url)
                .networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.OFFLINE)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE);
        try {
            return creator.get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void fetch (Context context, String url, Callback callback) {
        buildPicasso(context, url).fetch(callback);
    }

}
