package org.ipv8.attestation.restapi;

public interface AttestationRESTListener {
    public void onPeers(String s);
    public void onOutstanding(String s);
    public void onVerificationOutput(String s);
    public void onAttributes(String s);
}
