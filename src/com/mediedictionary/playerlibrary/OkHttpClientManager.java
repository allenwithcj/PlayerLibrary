package com.mediedictionary.playerlibrary;

import android.os.Handler;
import android.os.Looper;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpClientManager {
    /**
     * 请求时间
     */
    private static int TIMES = 30;
    private static OkHttpClientManager mInstance;
    private static OkHttpClient mOkHttpClient;
    private Handler mDelivery;

    public OkHttpClientManager() {
        mOkHttpClient = new OkHttpClient();
        // 获取UI主线程
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return Response
     */
    public Response _getOkHttp(String url) throws IOException {
        final Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        Response execute = call.execute();
        return execute;
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return 字符串
     */
    public String _getAsString(String url) throws IOException {
        Response execute = _getOkHttp(url);
        return execute.body().string();
    }

    /**
     * 异步的get请求
     *
     * @param url
     * @param callback
     */
    public void _getOkHttp(String url, final int code,
                           HashMap<String, String> map, final HttpCallback callback) {
        Request.Builder mBuilder = new Request.Builder();
        mBuilder.addHeader("apikey", "97e8d621474eea52233fd6a9f2b8727b");
        deliveryResult(callback, code, mBuilder
                .url(url).post(mapToParam(map)).build());
    }

    /**
     * 设置缓存的大小
     *
     * @param cacheSize
     */
	/*
	 * public void setCacheSize(Context context, int maxSize) { // TODO
	 * Auto-generated method stub File cacheDirectory = new File("cache"); if
	 * (!cacheDirectory.exists()) { cacheDirectory.mkdirs(); } Cache cache = new
	 * Cache(cacheDirectory, cacheSize); mOkHttpClient.setCache(cache);
	 * mOkHttpClient.setCache(new Cache(context.getExternalCacheDir()
	 * .getAbsoluteFile(), maxSize)); }
	 */

    /**
     * 异步的get请求
     *
     * @param url
     * @param callback
     */
    public void _getOkHttp(String url, final int code,
                           HashMap<String, String> map, final HttpCallback callback, int Times) {
        Request.Builder mBuilder = new Request.Builder();
        mOkHttpClient.setConnectTimeout(Times, TimeUnit.SECONDS);
        deliveryResult(callback, code, getRequest(mBuilder, url, map, callback));
    }

    private Request getRequest(Request.Builder mBuilder, String url,
                               HashMap<String, String> map, HttpCallback callback) {
        return mBuilder.url(url).post(mapToParam(map)).build();
    }

    private RequestBody mapToParam(HashMap<String, String> map) {
        // TODO Auto-generated method stub

        FormEncodingBuilder builder = new FormEncodingBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private void deliveryResult(final HttpCallback callback, final int code,
                                Request request) {
        SendStart(code, callback);
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                SendFailure(code, callback);
            }

            @Override
            public void onResponse(final Response response) {
                String string;
                try {
                    string = response.body().string();
                    SendSuccess(code, string, callback);
                    System.out.println("response: " + response);
                    System.out.println("response.cacheResponse: "
                            + response.cacheResponse());
                    System.out.println("response.networkResponse() "
                            + response.networkResponse());
                    ResetOption();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    SendFailure(code, callback);
                }

            }
        });
    }

    /**
     * 重置设置
     */
    public void ResetOption() {
        // TODO Auto-generated method stub
        /**
         * 超时重置
         */
        if (mOkHttpClient.getConnectTimeout() == TIMES) {
        } else {
            mOkHttpClient.setConnectTimeout(TIMES, TimeUnit.SECONDS);
        }

		/*	*//**
         * 缓存重置
         */
		/*
		 * try { mOkHttpClient.getCache().getSize();
		 * mOkHttpClient.setCache(null); } catch (Exception e) { // TODO: handle
		 * exception }
		 */

    }

    /**
     * 开始
     *
     * @param code
     * @param callback
     */
    public void SendStart(final int code, final HttpCallback callback) {
        // TODO Auto-generated method stub
        mDelivery.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                callback.onStart(code);
            }
        });
    }

    /**
     * 成功
     *
     * @param code
     * @param result
     * @param callback
     */
    public void SendSuccess(final int code, final String result,
                            final HttpCallback callback) {
        // TODO Auto-generated method stub
        mDelivery.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                callback.onSuccess(result, code);
            }
        });
    }

    /**
     * 结束
     *
     * @param code
     * @param callback
     */
    public void SendFailure(final int code, final HttpCallback callback) {
        // TODO Auto-generated method stub
        mDelivery.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                callback.onFailure(code);
            }
        });
    }

    public interface HttpCallback {
        /**
         *
         * @param result
         *            回调参数
         * @param code
         *            回调标记位
         */
        abstract void onSuccess(String result, int code);

        /**
         *
         * @param code
         *            回调标记位
         */
        abstract void onStart(int code);

        /**
         *
         * @param code
         *            回调标记位
         */
        abstract void onFailure(int code);

    }

}

