package org.ipv8.android.restapi;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class AttestationRESTInterface {

    private final AttestationRESTListener attestationRESTListener;

    public AttestationRESTInterface(AttestationRESTListener listener){
        attestationRESTListener = listener;
    }

    public void retrieve_peers(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "peers");

        SingleShotRequest request = new SingleShotRequest("attestation", "GET", map){
            protected void onPostExecute(String result) {
                attestationRESTListener.onPeers(result);
            }
        };
        request.execute();
    }

    public void retrieve_outstanding(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "outstanding");

        SingleShotRequest request = new SingleShotRequest("attestation", "GET", map){
            protected void onPostExecute(String result) {
                attestationRESTListener.onOutstanding(result);
            }
        };
        request.execute();
    }

    public void retrieve_verification_output(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "verification_output");

        SingleShotRequest request = new SingleShotRequest("attestation", "GET", map){
            protected void onPostExecute(String result) {
                attestationRESTListener.onVerificationOutput(result);
            }
        };
        request.execute();
    }

    public void retrieve_attributes(String mid){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "attributes");
        map.put("mid", mid);

        SingleShotRequest request = new SingleShotRequest("attestation", "GET", map){
            protected void onPostExecute(String result) {
                attestationRESTListener.onAttributes(result);
            }
        };
        request.execute();
    }

    public void retrieve_attributes(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "attributes");

        SingleShotRequest request = new SingleShotRequest("attestation", "GET", map){
            protected void onPostExecute(String result) {
                attestationRESTListener.onAttributes(result);
            }
        };
        request.execute();
    }

    public void put_request(String mid, String attribute_name, Map<String, String> metadata){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "request");
        map.put("mid", mid);
        map.put("attribute_name", attribute_name);
        if (metadata != null){
            Gson gson = new GsonBuilder().create();
            map.put("metadata", Base64.encodeToString(gson.toJson(metadata).getBytes(), Base64.DEFAULT));
        }

        SingleShotRequest request = new SingleShotRequest("attestation", "POST", map){
            protected void onPostExecute(String result) {
            }
        };
        request.execute();
    }

    public void put_attest(String mid, String attribute_name, String attribute_value){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "attest");
        map.put("mid", mid);
        map.put("attribute_name", attribute_name);
        map.put("attribute_value", attribute_value);

        SingleShotRequest request = new SingleShotRequest("attestation", "POST", map){
            protected void onPostExecute(String result) {
            }
        };
        request.execute();
    }

    public void put_verify(String mid, String attribute_hash, String[] attribute_values){
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "verify");
        map.put("mid", mid);
        map.put("attribute_hash", attribute_hash);
        String attribute_values_str = "";
        for (String s: attribute_values)
            attribute_values_str += ("".equals(attribute_values_str) ? "" : ",") + s;
        map.put("attribute_values", attribute_values_str);

        SingleShotRequest request = new SingleShotRequest("attestation", "POST", map){
            protected void onPostExecute(String result) {
            }
        };
        request.execute();
    }
}
