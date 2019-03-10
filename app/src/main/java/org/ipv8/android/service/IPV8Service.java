package org.ipv8.android.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.ipv8.android.R;
import org.kivy.android.PythonService;

public class IPV8Service extends PythonService {

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
        intent.putExtra("serviceDescription", "http://127.0.0.1:8090");
        intent.putExtra("serviceIconId", R.mipmap.ic_launcher);
        ctx.startService(intent);
    }

    public static void stop(Context ctx) {
        Intent intent = new Intent(ctx, IPV8Service.class);
        ctx.stopService(intent);
    }

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
