package org.ipv8.attestation.service;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.ipv8.android.R;
import org.ipv8.android.restapi.IRestApi;
import org.kivy.android.PythonService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class IPV8Service extends PythonService {

    public static IRestApi createService(final String baseUrl, final String authToken) {

        OkHttpClient.Builder okHttp = new OkHttpClient.Builder()
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor()) //DEBUG
                .retryOnConnectionFailure(true)
                .followSslRedirects(false)
                .followRedirects(false);

        return createService(baseUrl, authToken, okHttp);
    }

    public static IRestApi createService(final String baseUrl, final String authToken, OkHttpClient.Builder okHttp) {

        Retrofit.Builder retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl);

        if (!TextUtils.isEmpty(authToken)) {

            okHttp.addInterceptor(chain -> {
                Request request = chain.request();
                Request newReq = request.newBuilder()
                        .addHeader("Authorization", String.format("token %s", authToken))
                        .build();
                return chain.proceed(newReq);
            });
        }
        retrofit.client(okHttp.build());

        return retrofit.build().create(IRestApi.class);
    }

    public static IRestApi createService() {
        return IPV8Service.createService("http://127.0.0.1:8085", "");
    }

    public static void start(Context ctx) {
        String argument = ctx.getFilesDir().getAbsolutePath();
        Intent intent = new Intent(ctx, IPV8Service.class);
        intent.putExtra("androidPrivate", argument);
        intent.putExtra("androidArgument", argument);
        intent.putExtra("pythonName", "IPV8Service");
        intent.putExtra("pythonHome", argument);
        intent.putExtra("pythonPath", argument + ":" + argument + "/lib");
        intent.putExtra("pythonServiceArgument", "");
        intent.putExtra("serviceEntrypoint", "ipv8.py");
        intent.putExtra("serviceTitle", "IPv8 service");
        intent.putExtra("serviceDescription", ctx.getString(R.string.service_url) + ":"
                + ctx.getString(R.string.service_port_number));
        intent.putExtra("serviceIconId", R.mipmap.ic_stat_wallet);
        ctx.startService(intent);
    }

    public static void stop(Context ctx) {
        Intent intent = new Intent(ctx, IPV8Service.class);
        ctx.stopService(intent);
    }

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getStartForeground() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
