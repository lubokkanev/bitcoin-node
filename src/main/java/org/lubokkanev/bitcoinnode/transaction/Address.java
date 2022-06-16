package org.lubokkanev.bitcoinnode.transaction;

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
        // TODO: validate addresses
        this.cashAddress = cashAddress;
        this.pubKeyHash = pubKeyHash;

        if ((cashAddress == null || cashAddress.isEmpty()) && (pubKeyHash == null || pubKeyHash.isEmpty())) {
            throw new RuntimeException("Set at least one of the addresses.");
        }

        if (cashAddress == null || cashAddress.isEmpty()) {
            this.cashAddress = calculateCashAddress(pubKeyHash);
        }

        if (pubKeyHash == null || pubKeyHash.isEmpty()) {
            this.pubKeyHash = calculatePubKeyHash(cashAddress);
        }
    }

    private String calculateCashAddress(String pubKeyHash) {
        // TODO
        return "";
    }

    private String calculatePubKeyHash(String cashAddress) {
        // TODO
        return "";
    }
}
