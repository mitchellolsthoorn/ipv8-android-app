package org.ipv8.attestation.restapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AttestorInterface{

    static class AttestationRequest{
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
    private Lock attestationRequestLock = new ReentrantLock();
    private List<AttestorInterface.AttestationRequest> attestationRequestResult = null;

    public AttestorInterface(AttestationRESTInterface restInterface){
        this.restInterface = restInterface;
    }

    public List<AttestorInterface.AttestationRequest> getAttestationRequests(){
        this.restInterface.retrieve_outstanding();
        this.attestationRequestLock.lock();
        List<AttestorInterface.AttestationRequest> result = null;
        try {
            this.attestationRequestLock.tryLock(5, TimeUnit.SECONDS);
            result = this.attestationRequestResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = new ArrayList<AttestorInterface.AttestationRequest>();
        }
        return result;
    }

    public void onOutstanding(String s) {
        List<AttestorInterface.AttestationRequest> out = new ArrayList<AttestorInterface.AttestationRequest>();
        JsonArray list = (JsonArray) new JsonParser().parse(s);
        if(list != null && list.size() > 0) {
            for (JsonElement rawTuple : list) {
                JsonArray tuple = (JsonArray) rawTuple;
                out.add(new AttestorInterface.AttestationRequest(tuple.get(0).getAsString(), tuple.get(1).getAsString()));
            }
        }
        this.attestationRequestResult = out;
        this.attestationRequestLock.unlock();
    }

    public void sendAttestation(String identifier, String attributeName, String attributeValue){
        this.restInterface.put_attest(identifier, attributeName, attributeValue);
    }
}