package org.ipv8.android.restapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AttestorInterface{

    public static class AttestationRequest{
        private final String identifier;
        private final String attributeName;

        public AttestationRequest(String identifier, String attributeName){
            this.identifier = identifier;
            this.attributeName = attributeName;
        }

        public String getIdentifier(){
            return this.identifier;
        }

        public String getAttributeName(){
            return this.attributeName;
        }
    }

    private AttestationRESTInterface restInterface;
    private Semaphore attestationRequestLock = new Semaphore(1);
    private List<AttestorInterface.AttestationRequest> attestationRequestResult = null;

    public AttestorInterface(AttestationRESTInterface restInterface){
        this.restInterface = restInterface;
    }

    public List<AttestorInterface.AttestationRequest> getAttestationRequests(){
        try {
            this.attestationRequestLock.acquire();
        } catch (InterruptedException e){
            return null;
        }
        this.restInterface.retrieve_outstanding();
        List<AttestorInterface.AttestationRequest> result = null;
        try {
            this.attestationRequestLock.tryAcquire(5, TimeUnit.SECONDS);
            this.attestationRequestLock.release();
            result = this.attestationRequestResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = new ArrayList<AttestorInterface.AttestationRequest>();
        }
        return result;
    }

    public void onOutstanding(String s) {
        List<AttestorInterface.AttestationRequest> out = new ArrayList<AttestorInterface.AttestationRequest>();
        try {
            JsonArray list = (JsonArray) new JsonParser().parse(s);
            if (list != null && list.size() > 0) {
                for (JsonElement rawTuple : list) {
                    JsonArray tuple = (JsonArray) rawTuple;
                    out.add(new AttestorInterface.AttestationRequest(tuple.get(0).getAsString(), tuple.get(1).getAsString()));
                }
            }
        } catch (ClassCastException e) {
        }
        this.attestationRequestResult = out;
        this.attestationRequestLock.release();
    }

    public void sendAttestation(String identifier, String attributeName, String attributeValue){
        this.restInterface.put_attest(identifier, attributeName, attributeValue);
    }
}