package org.ipv8.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.ipv8.android.MainActivity;
import org.ipv8.android.MyUtils;
import org.kivy.android.PythonService;
import org.ipv8.android.R;

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
/*
        // Keep the CPU on
        PowerManager powerManager =
                (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Tribler");
        wakeLock.acquire();

        // Keep the Wi-Fi on
        WifiManager wifiManager =
                (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Tribler");
        wifiLock.acquire();
*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        //wakeLock.release();
        //wifiLock.release();
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
