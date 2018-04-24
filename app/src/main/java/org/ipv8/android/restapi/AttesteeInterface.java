package org.ipv8.android.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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

        public Attribute(String name, String hash){
            this.name = name;
            this.hash = hash;
        }

        public String getName(){
            return this.name;
        }

        public String getHash(){
            return this.hash;
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

    public List<String> getPeerIdentifiers(){
        try {
            this.getPeersLock.acquire();
        } catch (InterruptedException e){
            return null;
        }
        this.restInterface.retrieve_outstanding();
        List<String> result = null;
        try {
            this.getPeersLock.tryAcquire(5, TimeUnit.SECONDS);
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
        for (String mid: knownMids){
            restInterface.retrieve_attributes(mid);
        }
        this.getPeersResult = Arrays.asList(knownMids);
        this.getPeersLock.release();
    }

    public void requestAttestation(String identifier, String attributeName){
        this.restInterface.put_request(identifier, attributeName);
    }

    public List<AttesteeInterface.Attribute> getMyAttributes(){
        try {
            this.getMyAttributesLock.acquire();
        } catch (InterruptedException e){
            return null;
        }
        this.restInterface.retrieve_outstanding();
        List<AttesteeInterface.Attribute> result = null;
        try {
            this.getMyAttributesLock.tryAcquire(5, TimeUnit.SECONDS);
            this.getMyAttributesLock.release();
            result = this.getMyAttributesResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = new ArrayList<AttesteeInterface.Attribute>();
        }
        return result;
    }

    public void onAttributes(String s) {
        JsonArray list = (JsonArray) new JsonParser().parse(s);
        Map<Map.Entry<String, String>, String> attributeHashes = new HashMap<Map.Entry<String, String>, String>();
        ArrayList<AttesteeInterface.Attribute> out = new ArrayList<AttesteeInterface.Attribute>();
        if(list != null && list.size() > 0) {
            for (JsonElement rawTuple : list) {
                JsonArray tuple = (JsonArray) rawTuple;
                out.add(new AttesteeInterface.Attribute(tuple.get(0).getAsString(), tuple.get(1).getAsString()));
            }
        }
        this.getMyAttributesResult = out;
        this.getMyAttributesLock.release();
    }
}