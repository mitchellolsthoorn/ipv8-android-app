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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ipv8.android.restapi.AttesteeInterface;
import org.ipv8.android.service.IPV8Service;

import java.util.HashMap;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == WRITE_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
            }
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

        Thread t = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updatePeerCount();
                    updateAttributes();
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private void updateAttributes() {
        AttestationInterface api = new AttestationInterface();
        AttesteeInterface attesteeAPI = api.getAttesteeInterface();
        List<AttesteeInterface.Attribute> attributes = attesteeAPI.getMyAttributes();
        boolean hasAttribute = attributes == null ? false : attributes.size() > 0;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int color = hasAttribute ? Color.GREEN : Color.RED;
                ((Button) findViewById(R.id.button_id)).setBackgroundColor(color);
            }
        });
    }

    private void updatePeerCount() {
        AttestationInterface api = new AttestationInterface();
        AttesteeInterface attesteeAPI = api.getAttesteeInterface();
        List<String> identifiers = attesteeAPI.getPeerIdentifiers();
        int peerCount = identifiers == null ? 0 : identifiers.size();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.textView)).setText("Peers: " + peerCount);
            }
        });
    }

    public void buttonActivate(View v){
        Thread t = new Thread(){
            public void run(){
                AttestationInterface api = new AttestationInterface();
                AttesteeInterface attesteeAPI = api.getAttesteeInterface();

                List<String> identifiers = attesteeAPI.getPeerIdentifiers();
                if ((identifiers == null) || (identifiers.size() == 0)) {
                    // No other people found
                    Log.e("Peers", "No peers found!");
                    return;
                }

                HashMap<String, String> metadata = new HashMap<String, String>();
                metadata.put("psn", "1234567890");

                for (String id : identifiers)
                    attesteeAPI.requestAttestation(id, "QR", metadata);
            }
        };
        t.start();
    }
}