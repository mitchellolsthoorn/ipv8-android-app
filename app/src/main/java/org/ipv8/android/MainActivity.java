package org.ipv8.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import org.ipv8.android.restapi.AttesteeInterface;
import org.ipv8.android.service.IPV8Service;

import java.util.List;

public class MainActivity extends BaseActivity {

    public static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 110;

    static {
        // Backwards compatibility for vector graphics
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private void shutdown() {
        killService();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    protected void startService() {
        IPV8Service.start(this); // Run normally
    }

    protected void killService() {
        IPV8Service.stop(this);
    }

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {

            case Intent.ACTION_MAIN:
                // Handle intent only once
                intent.setAction(null);
                return;

            case Intent.ACTION_SHUTDOWN:
                // Handle intent only once
                intent.setAction(null);
                shutdown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Write permissions on sdcard?
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            startService();
        }
    }

    private void setActivateButton(boolean value, boolean success) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int color = success ? Color.GREEN : Color.RED;
                ((Button) findViewById(R.id.button_id)).setEnabled(value);
                if (value)
                    ((Button) findViewById(R.id.button_id)).setBackgroundColor(color);
            }
        });
    }

    public void buttonActivate(View v){
        setActivateButton(false, false);
        Thread t = new Thread(){
            public void run(){
                AttestationInterface api = new AttestationInterface();
                AttesteeInterface attesteeAPI = api.getAttesteeInterface();

                List<String> identifiers = attesteeAPI.getPeerIdentifiers();
                if ((identifiers == null) || (identifiers.size() == 0)) {
                    // No other people found
                    setActivateButton(true, false);
                    return;
                }

                for (String id : identifiers)
                    attesteeAPI.requestAttestation(id, "QR");

                Button button = ((Button) findViewById(R.id.button_id));
                button.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<AttesteeInterface.Attribute> attributes = attesteeAPI.getMyAttributes();
                        setActivateButton(true, attributes.size() > 0);
                    }
                }, 5000);
            }
        };
        t.start();
    }
}