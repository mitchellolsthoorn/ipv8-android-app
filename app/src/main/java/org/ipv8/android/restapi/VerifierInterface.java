package org.ipv8.android.restapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class VerifierInterface {

    static class Verification{
        private final String value;
        private final String match;

        public Verification(String value, String match){
            this.value = value;
            this.match = match;
        }

        public String getValue(){
            return this.value;
        }

        public String getMatch(){
            return this.match;
        }

        public boolean matches() {
            return Float.parseFloat(this.match) >= 0.99f;
        }
    }

    private AttestationRESTInterface restInterface;
    private Semaphore getVerificationsLock = new Semaphore(1);
    private Map<String, List<Verification>> getVerificationsResult = null;

    public VerifierInterface(AttestationRESTInterface restInterface){
        this.restInterface = restInterface;
    }

    public void requestVerification(String identifier, String attributeHash, List<String> attributeValues){
        this.restInterface.put_verify(identifier, attributeHash, attributeValues.toArray(new String[]{}));
    }

    public Map<String, List<Verification>> getVerifications(){
        try {
            this.getVerificationsLock.acquire();
        } catch (InterruptedException e){
            return null;
        }
        this.restInterface.retrieve_verification_output();
        Map<String, List<Verification>> result = null;
        try {
            this.getVerificationsLock.tryAcquire(30, TimeUnit.SECONDS);
            this.getVerificationsLock.release();
            result = this.getVerificationsResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = new HashMap<String, List<Verification>>();
        }
        return result;
    }

    public List<Verification> getVerification(String hash){
        Map<String, List<Verification>> verifications = getVerifications();
        return verifications.getOrDefault(hash, new ArrayList<Verification>());
    }

    public void onVerificationOutput(String s) {
        Map<String, List<VerifierInterface.Verification>> out = new HashMap<String, List<Verification>>();
        JsonObject rootlist = (JsonObject) new JsonParser().parse(s);
        for (Map.Entry<String, JsonElement> rawList : rootlist.entrySet()) {
            List<Verification> subout = new ArrayList<Verification>();
            JsonArray list = (JsonArray) rawList.getValue();
            if(list != null && list.size() > 0) {
                for (JsonElement rawTuple : list) {
                    JsonArray tuple = (JsonArray) rawTuple;
                    subout.add(new Verification(tuple.get(0).getAsString(), tuple.get(1).getAsString()));
                }
            }
            out.put(rawList.getKey(), subout);
        }
        this.getVerificationsResult = out;
        this.getVerificationsLock.release();
    }
}
