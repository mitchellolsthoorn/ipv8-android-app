package org.ipv8.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cantrowitz.rxbroadcast.RxBroadcast;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.elements.ObjectDescriptor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.ipv8.android.restapi.EventStream;
import org.ipv8.android.restapi.SingleShotRequest;
import org.ipv8.android.restapi.json.AttestationRESTInterface;
import org.ipv8.android.restapi.json.AttestationRESTListener;
import org.ipv8.android.service.IPV8Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements Handler.Callback, AttestationRESTListener {

    public static final int ADD_ACCOUNT_ACTIVITY_REQUEST_CODE = 103;
    public static final int INPUT_REQUIRED_ACTIVITY_REQUEST_CODE = 105;

    public static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 110;

    static {
        // Backwards compatibility for vector graphics
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.main_progress)
    View progressView;

    @BindView(R.id.main_progress_status)
    TextView statusBar;

    // GUI
    private ActionBarDrawerToggle _navToggle;
    private ConnectivityManager _connectivityManager;
    private Handler _eventHandler;
    private Role _role = Role.UNKNOWN;
    private boolean isLoading = true;

    // Service related
    private AttestationRESTInterface restInterface;
    private String[] knownMids; // List of mids
    private List<Map.Entry<String, String>> outstandingRequests; // List of (mid, attribute_name)
    private Map<String, List<Map.Entry<String, String>>> verificationOutput; // Map of attribute_hash -> [(value, match)]
    private Map<Map.Entry<String, String>, String> attributeHashes = new HashMap<Map.Entry<String, String>, String>();

    // Selection related
    private String subject;
    private String subjectAttribute;

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // clear all pending inputs
        SharedPreferences sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("pendingInputs", new HashSet<String>());
        editor.apply();

        // Hamburger icon
        _navToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(_navToggle);
        enableNavigationMenu(false);

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Stetho.initializeWithDefaults(getApplicationContext()); //DEBUG

        initConnectivityManager();

        // Start listening to events on the main thread so the gui can be updated
        _eventHandler = new Handler(Looper.getMainLooper(), this);
        EventStream.addHandler(_eventHandler);

        showLoading(R.string.status_opening_eventstream);
        EventStream.openEventStream();

        // Write permissions on sdcard?
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            startService();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        drawer.removeDrawerListener(_navToggle);
        EventStream.removeHandler(_eventHandler);
        super.onDestroy();
        _navToggle = null;
        _connectivityManager = null;
        _eventHandler = null;
    }

    private void setUnknownRole(boolean visible){
        View view = findViewById(R.id.unknown_role_text);
        if (view != null){
            if (visible)
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.GONE);
        }
    }

    private String toAttributeHash(String subject, String attributeName){
        return attributeHashes.get(new AbstractMap.SimpleEntry<String, String>(subject, attributeName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        if (isLoading){
            setUnknownRole(true);
            isLoading = false;
            showLoading(false);
            enableNavigationMenu(true);
            switchFragment(ListFragment.class);
            restInterface = new AttestationRESTInterface(this);
            return true;
        }
        return true;
    }

    public void enableNavigationMenu(boolean enabled) {
        if (enabled)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        else
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        _navToggle.setDrawerIndicatorEnabled(enabled);
        _navToggle.syncState();
    }

    /**
     * {@inheritDoc}
     */
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

                drawer.openDrawer(GravityCompat.START);
                return;

            case ConnectivityManager.CONNECTIVITY_ACTION:
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:

                // Warn user if connection is lost
                if (!MyUtils.isNetworkConnected(_connectivityManager)) {
                    Toast.makeText(MainActivity.this, R.string.warning_lost_connection, Toast.LENGTH_SHORT).show();
                }
                return;

            case NfcAdapter.ACTION_NDEF_DISCOVERED:
                Log.v("ACTION_NDEF_DISCOVERED", String.format("%b", intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)));

                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (rawMsgs != null && rawMsgs.length > 0) {
                    for (Parcelable rawMsg : rawMsgs) {
                        // Decode message
                        NdefRecord[] records = ((NdefMessage) rawMsg).getRecords();
                    }
                }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case ADD_ACCOUNT_ACTIVITY_REQUEST_CODE:
                // Update view
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof ListFragment) {
                    ((ListFragment) fragment).reload();
                }
                return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case WRITE_STORAGE_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService();
                } else {
                    finish();
                }
                return;
        }
        // Propagate results
        Fragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected void showLoading(@Nullable CharSequence text) {
        if (text == null) {
            progressView.setVisibility(View.GONE);
        } else {
            statusBar.setText(text);
            progressView.setVisibility(View.VISIBLE);
        }
    }

    protected void showLoading(boolean show) {
        showLoading(show ? "" : null);
    }

    protected void showLoading(@StringRes int resId) {
        showLoading(getText(resId));
    }

    private void initConnectivityManager() {
        _connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        Observer observer = new Observer<Intent>() {

            public void onNext(Intent intent) {
                handleIntent(intent);
            }

            public void onCompleted() {
            }

            public void onError(Throwable e) {
                Log.v("connectivityMgr", e.getMessage(), e);
            }
        };

        // Listen for connectivity changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
                .subscribe(observer));

        // Listen for network state changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
                .subscribe(observer));

        // Listen for Wi-Fi state changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
                .subscribe(observer));

        // Listen for Wi-Fi direct state changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION))
                .subscribe(observer));

        // Listen for Wi-Fi direct discovery changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION))
                .subscribe(observer));

        // Listen for Wi-Fi direct peer changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION))
                .subscribe(observer));

        // Listen for Wi-Fi direct connection changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION))
                .subscribe(observer));

        // Listen for Wi-Fi direct device changes
        rxSubs.add(RxBroadcast.fromBroadcast(this, new IntentFilter(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION))
                .subscribe(observer));
    }

    @Nullable
    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_main);
    }

    /**
     * @param newFragmentClass The desired fragment class
     * @return True if fragment is switched, false otherwise
     */
    public boolean switchFragment(Class newFragmentClass) {
        // Check if current fragment is desired fragment
        if (!newFragmentClass.isInstance(getCurrentFragment())) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Check if desired fragment is already instantiated
            String className = newFragmentClass.getName();
            Fragment fragment = fragmentManager.findFragmentByTag(className);
            if (fragment == null) {
                try {
                    fragment = (Fragment) newFragmentClass.newInstance();
                    fragment.setRetainInstance(true);
                } catch (InstantiationException ex) {
                    Log.e("switchFragment", className, ex);
                } catch (IllegalAccessException ex) {
                    Log.e("switchFragment", className, ex);
                }
            }
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_main, fragment, className)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * @return Fragment that was removed, if any
     */
    @Nullable
    private Fragment removeFragment() {
        Fragment fragment = getCurrentFragment();
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        return fragment;
    }

    public void navShutdownClicked(MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        Intent shutdown = new Intent(Intent.ACTION_SHUTDOWN);
        // Ask user to confirm shutdown
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_shutdown);
        builder.setPositiveButton(R.string.action_shutdown_short, (dialog, which) -> {
            onNewIntent(shutdown);
        });
        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> {
            // Do nothing
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateListItems(Iterable<Object> items){
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof ListFragment) {
            ListFragment listFragment = (ListFragment) fragment;
            listFragment.getAdapter().clear();
            for (Object s : items){
                listFragment.getAdapter().addObject(s);
            }
            listFragment.getAdapter().notifyDataSetChanged();
        }
    }

    public void viewPeerList(){
        updateListItems(new ArrayList<Object>());
        // TODO Adapt to role
        // TODO On click peer in list go to view*Dialog() with subject/subjectAttribute
    }

    public void viewAttestorDialog(){
        List<Object> list = Arrays.asList(new Object[] {
                new String[] {"2", "Someone wants you to measure something.", "Measurement:"},
                new String[] {"3", "Submit", "Ignore"}
        });
        updateListItems(list);
    }

    public void viewAttesteeDialog(){
        List<Object> list = Arrays.asList(new Object[] {
                new String[] {"2", "Ask someone to attest.", "Attribute:"},
                new String[] {"3", "Send", "Cancel"}
                //new String[] {"1", "Allow someone to verify something."}, // TODO
                //new String[] {"3", "Yes", "No"}
        });
        updateListItems(list);
    }

    public void viewVerifierDialog(){
        List<Object> list = Arrays.asList(new Object[] {
                new String[] {"2", "Ask someone to verify.", "Attribute:"},
                new String[] {"2", "Required value:"},
                new String[] {"3", "Send", "Cancel"}
        });
        updateListItems(list);
    }

    public void navAttestorClicked(MenuItem item) {
        setUnknownRole(false);
        Log.v("NAVIGATION", "Clicked Attestor!");
        _role = Role.ATTESTOR;
        viewPeerList();
        drawer.closeDrawer(GravityCompat.START);
    }

    public void navAttesteeClicked(MenuItem item) {
        setUnknownRole(false);
        Log.v("NAVIGATION", "Clicked Attestee!");
        _role = Role.ATTESTEE;
        viewPeerList();
        drawer.closeDrawer(GravityCompat.START);
    }

    public void navVerifierClicked(MenuItem item) {
        setUnknownRole(false);
        Log.v("NAVIGATION", "Clicked Verifier!");
        _role = Role.VERIFIER;
        viewPeerList();
        drawer.closeDrawer(GravityCompat.START);
    }

    public void onGenericButtonClicked(View view){
        FancyButton button = (FancyButton) view;
        switch (_role){
            case ATTESTOR:
                onAttestorButtonClicked(button.getText().toString());
                break;
            case ATTESTEE:
                onAttesteeButtonClicked(button.getText().toString());
                break;
            case VERIFIER:
                onVerifierButtonClicked(button.getText().toString());
                break;
        }
    }

    private List<String> getGenericInputs(){
        List<View> inputFields = getViewsByTag((ViewGroup) findViewById(R.id.fragment_main), "generic_text_input");
        ArrayList<String> out = new ArrayList<String>();
        for(View v: inputFields){
            out.add(((EditText) v).getText().toString());
        }
        return out;
    }

    private void onAttestorButtonClicked(String button){
        if ("Submit".equals(button)){
            List<String> inputs = getGenericInputs();
            if ((inputs.size() != 1) || ("".equals(inputs.get(0)))){
                Toast.makeText(MainActivity.this, "Invalid attestation input!", Toast.LENGTH_SHORT).show();
                return;
            }
            if ((knownMids == null) || (!Arrays.asList(knownMids).contains(subject))){
                Toast.makeText(MainActivity.this, "No valid subject selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            if ((subjectAttribute == null) || ("".equals(subjectAttribute))){
                Toast.makeText(MainActivity.this, "No valid attribute selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            String attestationValue = inputs.get(0);
            restInterface.put_attest(subject, subjectAttribute, attestationValue);
            viewPeerList();
        } else if ("Ignore".equals(button)){
            viewPeerList();
        }
    }

    private void onAttesteeButtonClicked(String button){
        if ("Send".equals(button)){
            List<String> inputs = getGenericInputs();
            if ((inputs.size() != 1) || ("".equals(inputs.get(0)))){
                Toast.makeText(MainActivity.this, "Invalid attestation input!", Toast.LENGTH_SHORT).show();
                return;
            }
            if ((knownMids == null) || (!Arrays.asList(knownMids).contains(subject))){
                Toast.makeText(MainActivity.this, "No valid subject selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            String attributeName = inputs.get(0);
            restInterface.put_request(subject, attributeName);
            viewPeerList();
        } else if ("Cancel".equals(button)){
            viewPeerList();
        }
    }

    private void onVerifierButtonClicked(String button){
        if ("Send".equals(button)){
            List<String> inputs = getGenericInputs();
            if ((inputs.size() != 2) || ("".equals(inputs.get(0)) || ("".equals(inputs.get(1))))){
                Toast.makeText(MainActivity.this, "Invalid attestation input!", Toast.LENGTH_SHORT).show();
                return;
            }
            if ((knownMids == null) || (!Arrays.asList(knownMids).contains(subject))){
                Toast.makeText(MainActivity.this, "No valid subject selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            String attributeName = inputs.get(0);
            String attributeHash = toAttributeHash(subject, attributeName);
            if ((attributeHash == null) || ("".equals(attributeHash))){
                Toast.makeText(MainActivity.this, "Could not link attribute name to hash!", Toast.LENGTH_SHORT).show();
                return;
            }
            String attributeValue = inputs.get(1);
            restInterface.put_verify(subject, attributeHash, new String[] {attributeValue});
            viewPeerList();
        } else if ("Cancel".equals(button)){
            viewPeerList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Context context = this;
    }

    private void shutdown() {
        // Clear view
        removeFragment();

        showLoading(R.string.status_shutting_down);

        EventStream.closeEventStream();
    }

    protected void startService() {
        IPV8Service.start(this); // Run normally
    }

    protected void killService() {
        IPV8Service.stop(this);
    }

    @Override
    public void onPeers(String s) {
        Gson gson = new Gson();
        knownMids = gson.fromJson(s, String[].class);
    }

    @Override
    public void onOutstanding(String s) {
        List<Map.Entry<String, String>> out = new ArrayList<Map.Entry<String, String>>();
        JsonArray list = (JsonArray) new JsonParser().parse(s);
        if(list != null && list.size() > 0) {
            for (JsonElement rawTuple : list) {
                JsonArray tuple = (JsonArray) rawTuple;
                out.add(new AbstractMap.SimpleImmutableEntry<String, String>(tuple.get(0).getAsString(), tuple.get(1).getAsString()));
            }
        }
        outstandingRequests = out;
    }

    @Override
    public void onVerificationOutput(String s) {
        Map<String, List<Map.Entry<String, String>>> out = new HashMap<String, List<Map.Entry<String, String>>>();
        JsonObject rootlist = (JsonObject) new JsonParser().parse(s);
        for (Map.Entry<String, JsonElement> rawList : rootlist.entrySet()) {
            List<Map.Entry<String, String>> subout = new ArrayList<Map.Entry<String, String>>();
            JsonArray list = (JsonArray) rawList.getValue();
            if(list != null && list.size() > 0) {
                for (JsonElement rawTuple : list) {
                    JsonArray tuple = (JsonArray) rawTuple;
                    subout.add(new AbstractMap.SimpleImmutableEntry<String, String>(tuple.get(0).getAsString(), tuple.get(1).getAsString()));
                }
            }
            out.put(rawList.getKey(), subout);
        }
        verificationOutput = out;
    }

    @Override
    public void onAttributes(String s) {
        // TODO: Upstream broken
    }
}