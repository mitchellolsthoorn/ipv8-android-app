package org.ipv8.android.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AttesteeInterface{

    public static class Attribute{
        private final String name;
        private final String hash;
        private final Map<String, String> metadata;

        public Attribute(String name, String hash, Map<String, String> metadata){
            this.name = name;
            this.hash = hash;
            this.metadata = metadata;
        }

        public String getName(){
            return this.name;
        }

        public String getHash(){
            return this.hash;
        }

        public Map<String, String> getMetadata(){
            return this.metadata;
        }
    }

    private AttestationRESTInterface restInterface;
    private Semaphore getPeersLock = new Semaphore(1);
    private List<String> getPeersResult = null;
    private Semaphore getMyAttributesLock = new Semaphore(1);
    private List<AttesteeInterface.Attribute> getMyAttributesResult = null;

    public AttesteeInterface(AttestationRESTInterface restInterface){
        this.restInterface = restInterface;
    }

    /**
     * Completely remove all of our gathered identity data.
     */
    public void dropIdentity(){
        this.restInterface.drop_identity();
    }

    public List<String> getPeerIdentifiers(){
        try {
            this.getPeersLock.acquire();
        } catch (InterruptedException e){
            return null;
        }
        this.restInterface.retrieve_peers();
        List<String> result = null;
        try {
            this.getPeersLock.tryAcquire(30, TimeUnit.SECONDS);
            this.getPeersLock.release();
            result = this.getPeersResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = new ArrayList<String>();
        }
        return result;
    }

    public void onPeers(String s) {
        Gson gson = new Gson();
        String[] knownMids = gson.fromJson(s, String[].class);
        if (knownMids != null){
            for (String mid: knownMids){
                restInterface.retrieve_attributes(mid);
            }
            this.getPeersResult = Arrays.asList(knownMids);
        } else {
            this.getPeersResult = new ArrayList<String>();
        }
        this.getPeersLock.release();
    }

    public void requestAttestation(String identifier, String attributeName){
        this.restInterface.put_request(identifier, attributeName, null);
    }

    public void requestAttestation(String identifier, String attributeName, Map<String, String> metadata){
        this.restInterface.put_request(identifier, attributeName, metadata);
    }

    public List<AttesteeInterface.Attribute> getMyAttributes(){
        try {
            this.getMyAttributesLock.acquire();
        } catch (InterruptedException e){
            return null;
        }
        this.restInterface.retrieve_attributes();
        List<AttesteeInterface.Attribute> result = null;
        try {
            this.getMyAttributesLock.tryAcquire(30, TimeUnit.SECONDS);
            this.getMyAttributesLock.release();
            result = this.getMyAttributesResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = new ArrayList<AttesteeInterface.Attribute>();
        }
        return result;
    }

    public void onAttributes(String s) {
        JsonElement element = new JsonParser().parse(s);
        if (element instanceof JsonArray) {
            JsonArray list = (JsonArray) element;
            ArrayList<AttesteeInterface.Attribute> out = new ArrayList<AttesteeInterface.Attribute>();
            if (list != null && list.size() > 0) {
                for (JsonElement rawTuple : list) {
                    JsonArray tuple = (JsonArray) rawTuple;
                    HashMap<String, String> metadata = new HashMap<String, String>();
                    for (Map.Entry<String, JsonElement> entry: tuple.get(2).getAsJsonObject().entrySet()){
                        metadata.put(entry.getKey(), entry.getValue().getAsString());
                    }
                    out.add(new AttesteeInterface.Attribute(tuple.get(0).getAsString(),
                            tuple.get(1).getAsString(),
                            metadata
                            ));
                }
            }
            this.getMyAttributesResult = out;
        }
        this.getMyAttributesLock.release();
    }
}