package com.digitalsign.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor

public class SignatureInfo {
    private String signerName;
    private String reason;
    private String location;
    private String contactInfo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signingTime;

    private String hashAlgorithm;
    private String signatureAlgorithm;
    private CertificateInfo certificate;
    private VerificationStatus status;
    private String statusMessage;

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }
}
