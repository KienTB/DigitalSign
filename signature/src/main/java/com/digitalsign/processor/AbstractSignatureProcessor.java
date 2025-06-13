package com.digitalsign.processor;

import com.digitalsign.exception.SignatureVerificationException;
import com.digitalsign.model.CertificateInfo;
import com.digitalsign.model.SignatureInfo;
import com.digitalsign.model.SignatureVerificationResult;
import com.digitalsign.model.VerificationStatus;
import com.digitalsign.util.CertificateUtils;
import com.digitalsign.util.CryptoUtils;
import com.digitalsign.util.FileUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.*;

public abstract class AbstractSignatureProcessor implements SignatureProcessor {
    protected final Logger logger = LoggerFactory.getLogger(AbstractSignatureProcessor.class);

    @Autowired
    protected CertificateUtils certificateUtils;

    @Autowired
    protected CryptoUtils cryptoUtils;

    @Autowired
    protected FileUtils fileUtils;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public SignatureVerificationResult verifySignatures(byte[] content, String fileName) throws Exception {
        logger.info("Starting signature verification for file: {}", fileName);

        if (!validateFileFormat(content, fileName)) {
            return createErrorResult("Invalid file format for processor: " + getProcessorName(), fileName, content);
        }

        try {
            List<byte[]> signatureDataList = extractSignatureData(content);

            if (signatureDataList.isEmpty()) {
                return createNoSignatureResult(fileName, content);
            }

            byte[] originalContent = extractOriginalContent(content);
            SignatureVerificationResult result = createBaseResult(fileName, content);

            boolean allValid = true;

            for (int i = 0; i < signatureDataList.size(); i++) {
                try {
                    SignatureInfo sigInfo = processSignature(signatureDataList.get(i), originalContent, i);
                    result.addSignature(sigInfo);

                    if (sigInfo.getStatus() != VerificationStatus.VALID) {
                        allValid = false;
                    }
                } catch (Exception e) {
                    logger.error("Error processing signature {}: {}", i, e.getMessage());
                    SignatureInfo errorSig = createErrorSignature(i, e.getMessage());
                    result.addSignature(errorSig);
                    allValid = false;
                }
            }

            result.updateOverallStatus();
            result.setMessage(allValid ? "All signatures are valid" : "Some signatures failed validation");

            return result;
        } catch (Exception e) {
            logger.error("Error during signature verification: {}", e.getMessage());
            return createErrorResult("Signature verification failed: " + e.getMessage(), fileName, content);
        }
    }

    protected abstract List<byte[]> extractSignatureData(byte[] content) throws Exception;

    protected abstract byte[] extractOriginalContent(byte[] content) throws Exception;

    public abstract boolean validateFileFormat(byte[] content, String fileName);

    public abstract String getProcessorName();

    protected SignatureInfo processSignature(byte[] signatureData, byte[] originalContent, int index) throws Exception {
        CMSSignedData cmsSignedData = new CMSSignedData(signatureData);

        SignerInformationStore signers = cmsSignedData.getSignerInfos();
        Collection<SignerInformation> signerCollection = signers.getSigners();

        if (signerCollection.isEmpty()) {
            throw new Exception("No signers found in signature");
        }

        SignerInformation signer = signerCollection.iterator().next();

        Store certStore = cmsSignedData.getCertificates();
        Collection<X509CertificateHolder> certCollection = certStore.getMatches(signer.getSID());

        if (certCollection.isEmpty()) {
            throw new Exception("No certificate found for signer");
        }

        X509CertificateHolder certHolder = certCollection.iterator().next();
        X509Certificate certificate = certificateUtils.convertToX509Certificate(certHolder);

        boolean signatureValid = verifyCMSSignature(signer, certHolder, originalContent);

        CertificateInfo certInfo = certificateUtils.extractCertificateInfo(certificate);

        SignatureInfo sigInfo = new SignatureInfo();
        sigInfo.setSignerName(certInfo.getSubjectName());
        sigInfo.setSigningTime(extractSigningTime(signer));
        sigInfo.setHashAlgorithm(cryptoUtils.extractHashAlgoristhm(signer.getDigestAlgOID()));
        sigInfo.setSignatureAlgorithm(cryptoUtils.normalizeAlgoristhm(signer.getEncryptionAlgOID()));
        sigInfo.setCertificate(certInfo);
        sigInfo.setStatus(signatureValid ? VerificationStatus.VALID : VerificationStatus.INVALID);
        sigInfo.setStatusMessage(signatureValid ? "Signature is valid" : "Signature validation failed");

        performAdditionalValidation(sigInfo, certificate, signer);

        return sigInfo;
    }

    protected boolean verifyCMSSignature(SignerInformation signer, X509CertificateHolder certHolder, byte[] originalContent) {
        try {
            SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                    .setProvider("BC")
                    .build(certHolder);

            boolean signatureValid = signer.verify(verifier);

            if (signatureValid && originalContent != null) {
                String hashAlg = cryptoUtils.extractHashAlgoristhm(signer.getDigestAlgOID());
                byte[] calculatedHash = cryptoUtils.calculateHash(originalContent, hashAlg);
            }

            return signatureValid;
        } catch (Exception e) {
            logger.error("CMS signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    protected LocalDateTime extractSigningTime(SignerInformation signer) {
        try {
            AttributeTable signedAttributes = signer.getSignedAttributes();
            if (signedAttributes != null) {
                Attribute timeAttr = signedAttributes.get(CMSAttributes.signingTime);
                if (timeAttr != null) {
                    Time time = Time.getInstance(timeAttr.getAttrValues().getObjectAt(0));
                    return LocalDateTime.now();
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract signing time: {}", e.getMessage());
        }
        return null;
    }

    protected void performAdditionalValidation(SignatureInfo sigInfo, X509Certificate certificate, SignerInformation signer) {
        boolean certValid = certificateUtils.isCertificateValid(certificate);
        if (!certValid) {
            sigInfo.setStatus(VerificationStatus.INVALID);
            sigInfo.setStatusMessage("Certificate is not valid: " +
                    certificateUtils.getCertificateStatusDescription(certificate));
        }
    }

    protected SignatureVerificationResult createBaseResult(String fileName, byte[] content) {
        String fileType = fileUtils.getFileExtension(fileName);
        SignatureVerificationResult result = new SignatureVerificationResult(fileName, fileType);
        result.setFileSize(content.length);

        try {
            result.setFileHash(cryptoUtils.bytesToHex(cryptoUtils.calculateSHA256(content)));
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error calculating file hash: {}", e.getMessage());
            result.setFileHash("HASH_CALCULATION_FAILED");
        }

        return result;
    }

    protected SignatureVerificationResult createNoSignatureResult(String fileName, byte[] content) {
        SignatureVerificationResult result = createBaseResult(fileName, content);
        result.setHasSignature(false);
        result.setSignatureCount(0);
        result.setOverallStatus(VerificationStatus.INVALID);
        result.setMessage("No signatures found in the file");
        return result;
    }

    protected SignatureVerificationResult createErrorResult(String errorMessage, String fileName, byte[] content) {
        SignatureVerificationResult result = createBaseResult(fileName, content);
        result.setOverallStatus(VerificationStatus.UNKNOWN);
        result.setMessage(errorMessage);
        return result;
    }

    protected SignatureInfo createErrorSignature(int index, String errorMessage) {
        SignatureInfo sigInfo = new SignatureInfo();
        sigInfo.setStatus(VerificationStatus.UNKNOWN);
        sigInfo.setStatusMessage("Signature " + index + " processing failed: " + errorMessage);
        return sigInfo;
    }
}