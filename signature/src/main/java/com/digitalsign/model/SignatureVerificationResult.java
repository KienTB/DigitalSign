package com.digitalsign.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

public class SignatureVerificationResult {
    private String fileName;
    private String fileType;
    private long fileSize;
    private String fileHash;
    private boolean hasSignature;
    private int signatureCount;
    private List<SignatureInfo> signatures;
    private VerificationStatus overallStatus;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verificationTime;

    public SignatureVerificationResult() {
        this.signatures = new ArrayList<>();
        this.verificationTime = LocalDateTime.now();
    }

    public SignatureVerificationResult(String fileName, String fileType) {
        this();
        this.fileName = fileName;
        this.fileType = fileType;
    }

    //Convenience methods
    // thêm chữ ký
    public void addSignature(SignatureInfo signature) {
        this.signatures.add(signature);
        this.signatureCount = this.signatures.size();
        this.hasSignature = true;
    }

    // cập nhật trạng thái tổng thể chữ ký
    public void updateOverallStatus() {
        if (signatures.isEmpty()) {
            this.overallStatus = VerificationStatus.UNKNOWN;
            return;
        }

        boolean allValid = signatures.stream()
                .allMatch(sig -> sig.getStatus() == VerificationStatus.VALID);

        if (allValid) {
            this.overallStatus = VerificationStatus.VALID;
        } else {
            this.overallStatus = signatures.stream()
                    .map(SignatureInfo::getStatus)
                    .reduce(VerificationStatus.VALID, this::getWorseStatus);
        }
    }

    // hỗ trợ tìm trạng thái chữ ký tệ hơn trong hai trạng thái danh sách
    public VerificationStatus getWorseStatus(VerificationStatus a, VerificationStatus b) {
        int[] priority = {0,3,4,2,1,5};
        return priority[a.ordinal()] > priority[b.ordinal()] ? a : b;
    }

    public List<SignatureInfo> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<SignatureInfo> signatures) {
        this.signatures = signatures;
        this.signatureCount = signatures.size();
        this.hasSignature = !signatures.isEmpty();
    }
}
