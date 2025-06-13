package com.digitalsign.util;

import com.digitalsign.model.CertificateInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jcajce.provider.asymmetric.X509;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class CertificateUtils {
    @Autowired
    private CryptoUtils cryptoUtils;

    public CertificateInfo extractCertificateInfo(X509Certificate certificate) throws Exception {
        CertificateInfo info = new CertificateInfo();

        info.setSubjectName(extractCommonName(certificate.getSubjectX500Principal().getName()));
        info.setIssuerName(extractCommonName(certificate.getIssuerX500Principal().getName()));
        info.setSerialNumber(certificate.getSerialNumber().toString(16).toUpperCase());
        info.setAlgoristhm(certificate.getSigAlgName());

        info.setValidFrom(convertToLocalDateTime(certificate.getNotBefore()));
        info.setValidTo(convertToLocalDateTime(certificate.getNotAfter()));

        info.setThumbprint(cryptoUtils.getCertificateThumbPrint(certificate));

        info.setKeyUsage(extractKeyUsage(certificate));

        return info;
    }

    public String extractCommonName(String distinguishedName){
        try{
            X500Name x500Name = new X500Name(distinguishedName);
            return IETFUtils.valueToString(x500Name.getRDNs(BCStyle.CN)[0].getFirst().getValue());
        } catch (Exception e){
            String[] parts = distinguishedName.split(",");
            for(String part : parts){
                String trimmed = part.trim();
                if(trimmed.startsWith("CN=")){
                    return trimmed.substring(3);
                }
            }
            return distinguishedName;
        }
    }

    public String extractOrganization(String distinguishedName){
        try{
            X500Name x500Name = new X500Name(distinguishedName);
            return IETFUtils.valueToString(x500Name.getRDNs(BCStyle.O)[0].getFirst().getValue());
        } catch (Exception e){
            return null;
        }
    }

    public boolean isCertificateValid(X509Certificate certificate){
        try{
            certificate.checkValidity();
            return true;
        } catch ( Exception e){
            return false;
        }
    }

    public boolean isCertificateValidAt(X509Certificate certificate, Date date){
        try{
            certificate.checkValidity(date);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    private LocalDateTime convertToLocalDateTime(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String extractKeyUsage(X509Certificate certificate){
        boolean[] keyUsage = certificate.getKeyUsage();
        if (keyUsage == null) return "Not specified";

        StringBuilder usage = new StringBuilder();
        String[] keyUsageNames = {
                "Digital Signature", "Non Repudiation", "Key Encipherment", "Data Encipherment",
                "Key Agreement", "Key Cert Sign", "CRL Sign", "Encipher Only", "Decipher Only"
        };

        for(int i=0; i < keyUsage.length && i < keyUsageNames.length; i++){
            if (keyUsage[i]){
                if (usage.length() > 0) usage.append(", ");
                usage.append(keyUsageNames[i]);
            }
        }
        return usage.length() > 0 ? usage.toString() : "Not specified";
    }

    public String getCertificateStatusDescription(X509Certificate certificate){
        if (!isCertificateValid(certificate)){
            Date now = new Date();
            if (now.before(certificate.getNotBefore())){
                return "Certificate not yet valid";
            } else if (now.after(certificate.getNotAfter())){
                return "Certificate exprired";
            }
        }
        return "Certificate valid";
    }

    public String extractEmailFromCertificate(X509Certificate certificate){
        try{
            if (certificate.getSubjectAlternativeNames() != null){
                for(List<?> sanObj : certificate.getSubjectAlternativeNames()){
                    if(sanObj instanceof List<?>){
                        List<?> san = (java.util.List<?>) sanObj;
                        if (san.size() >= 2 && san.get(0).equals(1)){
                            return san.get(1).toString();
                        }
                    }
                }
            }
        }catch (Exception e){

        }
        String subjectDN = certificate.getSubjectX500Principal().getName();
        String[] parts = subjectDN.split(",");
        for(String part : parts){
            String trimmed = part.trim();
            if(trimmed.startsWith("EMAILADDRESS=") || trimmed.startsWith("E=")){
                return trimmed.substring(trimmed.indexOf('=') + 1);
            }
        }
        return null;
    }

    public X509Certificate convertToX509Certificate(X509CertificateHolder certHolder) throws Exception {
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
    }
}
