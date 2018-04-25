package org.ipv8.android.restapi;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class SingleShotRequest extends AsyncTask<Object, Object, String> {

    private static final String BASE_URL = "http://127.0.0.1:8085";

    private final OkHttpClient client; // = new OkHttpClient();
    private final Request request;

    public SingleShotRequest(String endpoint, String method, Map<String, String> values) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS);
        client = clientBuilder.build();

        Request.Builder req_builder = new Request.Builder();

        RequestBody body = null;
        if ("POST".equals(method))
            body = RequestBody.create(MediaType.parse("text/plain"), "");

        req_builder.method(method, body);

        HttpUrl.Builder http_builder = new HttpUrl.Builder();
        http_builder.scheme("http");
        http_builder.host("127.0.0.1");
        http_builder.port(8085);
        http_builder.addPathSegment(endpoint);
        for (Map.Entry<String, String> entry: values.entrySet()){
            http_builder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        HttpUrl url = http_builder.build();

        req_builder.url(url);
        request = req_builder.build();
    }

    @Override
    protected String doInBackground(Object[] params) {
        String out = "";
        try {
            Response response = client.newCall(request).execute();
            out = response.body().string();
        } catch (IOException e) {
            Log.e("SingleShotRequest", "" + e);
        }
        return out;
    }
}
