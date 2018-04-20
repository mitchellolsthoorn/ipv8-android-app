package org.ipv8.attestation.restapi;

public class AttestationInterface implements AttestationRESTListener{

    private AttestationRESTInterface restInterface;
    private AttestorInterface attestorInterface;
    private AttesteeInterface attesteeInterface;

    public AttestationInterface(){
        this.restInterface = new AttestationRESTInterface(this);
        this.attestorInterface = new AttestorInterface(restInterface);
        this.attesteeInterface = new AttesteeInterface(restInterface);
    }

    @Override
    public void onPeers(String s) {
        this.attesteeInterface.onPeers(s);
    }

    @Override
    public void onOutstanding(String s) {
        this.attestorInterface.onOutstanding(s);
    }

    @Override
    public void onVerificationOutput(String s) {
        // TODO
    }

    @Override
    public void onAttributes(String s) {
        this.attesteeInterface.onAttributes(s);
    }
}
