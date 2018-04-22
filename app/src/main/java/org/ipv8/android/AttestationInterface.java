package org.ipv8.android;

import org.ipv8.android.restapi.AttestationRESTInterface;
import org.ipv8.android.restapi.AttestationRESTListener;
import org.ipv8.android.restapi.AttesteeInterface;
import org.ipv8.android.restapi.AttestorInterface;
import org.ipv8.android.restapi.VerifierInterface;

public class AttestationInterface implements AttestationRESTListener {

    private AttestationRESTInterface restInterface;
    private AttestorInterface attestorInterface;
    private AttesteeInterface attesteeInterface;
    private VerifierInterface verifierInterface;

    public AttestationInterface(){
        this.restInterface = new AttestationRESTInterface(this);
        this.attestorInterface = new AttestorInterface(restInterface);
        this.attesteeInterface = new AttesteeInterface(restInterface);
        this.verifierInterface = new VerifierInterface(restInterface);
    }

    public AttestorInterface getAttestorInterface() {
        return attestorInterface;
    }

    public AttesteeInterface getAttesteeInterface() {
        return attesteeInterface;
    }

    public VerifierInterface getVerifierInterface() {
        return verifierInterface;
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
    public void onVerificationOutput(String s) { this.verifierInterface.onVerificationOutput(s); }

    @Override
    public void onAttributes(String s) {
        this.attesteeInterface.onAttributes(s);
    }
}
