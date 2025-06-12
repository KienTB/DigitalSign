package com.digitalsign.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationStatus {
    VALID("Valid"),
    EXPIRED("Expired"),
    REVOKED("Revoked"),
    INVALID("Invalid"),
    CORRUPTED("Corrupted Signature"),
    UNKNOWN("Unknown");

    private final String description;
}
