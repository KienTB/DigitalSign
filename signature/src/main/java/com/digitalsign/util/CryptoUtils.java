package com.digitalsign.util;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.HexFormat;

@Component
public class CryptoUtils {
    static {
        if(Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
    }

    public byte[] calculateHash(byte[] data, String algoristhm) throws NoSuchAlgorithmException{
        MessageDigest digest = MessageDigest.getInstance(algoristhm);
        return digest.digest(data);
    }

    public byte[] calculateSHA256(byte[] data) throws NoSuchAlgorithmException{
        return calculateHash(data, "SHA-256");
    }

    public byte[] calculateSHA1(byte[] data) throws NoSuchAlgorithmException{
        return calculateHash(data, "SHA-1");
    }

    public String bytesToHex(byte[] bytes){
        return HexFormat.of().formatHex(bytes);
    }

    public byte[] hexToBytes(String hex){
        return HexFormat.of().parseHex(hex);
    }

    public boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey, String algoristhm)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        Signature sig = Signature.getInstance(algoristhm);
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

    public String getCertificateThumbPrint(X509Certificate certificate) throws Exception{
        byte[] encoded = certificate.getEncoded();
        byte[] hash = calculateSHA1(encoded);
        return bytesToHex(hash);
    }

    public String normalizeAlgoristhm(String algoristhm) {
        if (algoristhm == null) return "UNKNOWN";

        String normalized = algoristhm.toUpperCase().trim();

        switch (normalized){
            case "SHA256WITHRSA":
            case "SHA256_WITH_RSA":
                return "SHA256withRSA";
            case "SHA1WITHRSA":
            case "SHA1_WITH_RSA":
                return "SHA1withRSA";
            case "SHA512WITHRSA":
            case "SHA512_WITH_RSA":
                return "SHA512withRSA";
            default:
                return normalized;
        }
    }

    public String extractHashAlgoristhm(String signatureAlgoristhm){
        if(signatureAlgoristhm == null) return "UNKNOWN";

        String normalized = signatureAlgoristhm.toUpperCase();
        if(normalized.contains("SHA256")) return "SHA-256";
        if(normalized.contains("SHA1")) return "SHA-1";
        if(normalized.contains("SHA512")) return "SHA-512";
        if(normalized.contains("MD5")) return "MD5";

        return "UNKNOWN";
    }
}
