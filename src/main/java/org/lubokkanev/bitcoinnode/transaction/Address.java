package org.lubokkanev.bitcoinnode.transaction;

import java.util.Objects;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class Address {
    public String getCashAddress() {
        return cashAddress;
    }

    public String getPubKeyHash() {
        return pubKeyHash;
    }

    private String cashAddress;
    private String pubKeyHash;

    public Address(String cashAddress, String pubKeyHash) {
        if ((cashAddress == null || cashAddress.isEmpty()) && (pubKeyHash == null || pubKeyHash.isEmpty())) {
            throw new RuntimeException("Set at least one of the addresses.");
        }

        // Validate the provided addresses
        if (cashAddress != null && !cashAddress.isEmpty()) {
            validateCashAddress(cashAddress);
        }
        if (pubKeyHash != null && !pubKeyHash.isEmpty()) {
            validatePubKeyHash(pubKeyHash);
        }

        this.cashAddress = cashAddress;
        this.pubKeyHash = pubKeyHash;

        if (cashAddress == null || cashAddress.isEmpty()) {
            this.cashAddress = calculateCashAddress(pubKeyHash);
        }

        if (pubKeyHash == null || pubKeyHash.isEmpty()) {
            this.pubKeyHash = calculatePubKeyHash(cashAddress);
        }
    }

    private String calculateCashAddress(String pubKeyHash) {
        if (pubKeyHash == null || pubKeyHash.isEmpty()) {
            return "";
        }
        
        // Simplified cash address generation from pubKeyHash
        // In reality, this would involve proper Base58 encoding with checksums
        // For this implementation, we'll use a simplified format
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pubKeyHash.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex and prefix with "bitcoincash:"
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "bitcoincash:" + hexString.substring(0, 34); // Take first 34 chars for address
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate cash address", e);
        }
    }

    private String calculatePubKeyHash(String cashAddress) {
        if (cashAddress == null || cashAddress.isEmpty()) {
            return "";
        }
        
        // Simplified pubKeyHash extraction from cash address
        // In reality, this would involve proper Base58 decoding and validation
        try {
            String addressPart = cashAddress.replace("bitcoincash:", "").replace("bchtest:", "");
            
            // For this simplified implementation, we'll reverse-engineer a pubKeyHash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(addressPart.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.substring(0, 40); // Take first 40 chars for pubKeyHash (160 bits)
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract pubKeyHash from cash address", e);
        }
    }

    private void validateCashAddress(String cashAddress) {
        if (cashAddress == null || cashAddress.isEmpty()) {
            throw new RuntimeException("Cash address cannot be null or empty");
        }
        
        // Allow special addresses like "coinbase"
        if ("coinbase".equals(cashAddress)) {
            return;
        }
        
        // Basic format validation for Bitcoin Cash addresses
        Pattern cashAddressPattern = Pattern.compile("^(bitcoincash:|bchtest:)?[qpzry9x8gf2tvdw0s3jn54khce6mua7l]{42,}$");
        Pattern legacyPattern = Pattern.compile("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$");
        
        if (!cashAddressPattern.matcher(cashAddress).matches() && !legacyPattern.matcher(cashAddress).matches()) {
            throw new RuntimeException("Invalid cash address format: " + cashAddress);
        }
    }

    private void validatePubKeyHash(String pubKeyHash) {
        if (pubKeyHash == null || pubKeyHash.isEmpty()) {
            throw new RuntimeException("PubKeyHash cannot be null or empty");
        }
        
        // Allow special addresses like "coinbase"
        if ("coinbase".equals(pubKeyHash)) {
            return;
        }
        
        // Validate that pubKeyHash is a valid hexadecimal string of the correct length
        Pattern hexPattern = Pattern.compile("^[a-fA-F0-9]{40}$"); // 160 bits = 40 hex chars
        if (!hexPattern.matcher(pubKeyHash).matches()) {
            throw new RuntimeException("Invalid pubKeyHash format: must be 40 hexadecimal characters");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Address address = (Address) o;
        return Objects.equals(cashAddress, address.cashAddress) && Objects.equals(pubKeyHash, address.pubKeyHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cashAddress, pubKeyHash);
    }
}
