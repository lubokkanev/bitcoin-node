package org.lubokkanev.bitcoinnode.transaction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void createAddress_WithValidCashAddress_ShouldSucceed() {
        String cashAddress = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String pubKeyHash = "1234567890123456789012345678901234567890";
        
        Address address = new Address(cashAddress, pubKeyHash);
        
        assertEquals(cashAddress, address.getCashAddress());
        assertEquals(pubKeyHash, address.getPubKeyHash());
    }

    @Test
    void createAddress_WithValidLegacyAddress_ShouldSucceed() {
        String legacyAddress = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa";
        String pubKeyHash = "1234567890123456789012345678901234567890";
        
        Address address = new Address(legacyAddress, pubKeyHash);
        
        assertEquals(legacyAddress, address.getCashAddress());
        assertEquals(pubKeyHash, address.getPubKeyHash());
    }

    @Test
    void createAddress_WithOnlyCashAddress_ShouldGeneratePubKeyHash() {
        String cashAddress = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        
        Address address = new Address(cashAddress, null);
        
        assertEquals(cashAddress, address.getCashAddress());
        assertNotNull(address.getPubKeyHash());
        assertFalse(address.getPubKeyHash().isEmpty());
        assertEquals(40, address.getPubKeyHash().length()); // 160 bits = 40 hex chars
    }

    @Test
    void createAddress_WithOnlyPubKeyHash_ShouldGenerateCashAddress() {
        String pubKeyHash = "1234567890123456789012345678901234567890";
        
        Address address = new Address(null, pubKeyHash);
        
        assertEquals(pubKeyHash, address.getPubKeyHash());
        assertNotNull(address.getCashAddress());
        assertFalse(address.getCashAddress().isEmpty());
        assertTrue(address.getCashAddress().startsWith("bitcoincash:"));
    }

    @Test
    void createAddress_WithTestNetCashAddress_ShouldSucceed() {
        String testnetAddress = "bchtest:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String pubKeyHash = "1234567890123456789012345678901234567890";
        
        Address address = new Address(testnetAddress, pubKeyHash);
        
        assertEquals(testnetAddress, address.getCashAddress());
        assertEquals(pubKeyHash, address.getPubKeyHash());
    }

    @Test
    void createAddress_WithNullBothParameters_ShouldFail() {
        assertThrows(RuntimeException.class, () -> new Address(null, null));
    }

    @Test
    void createAddress_WithEmptyBothParameters_ShouldFail() {
        assertThrows(RuntimeException.class, () -> new Address("", ""));
    }

    @Test
    void createAddress_WithInvalidCashAddressFormat_ShouldFail() {
        String invalidCashAddress = "invalid_address_format";
        String pubKeyHash = "1234567890123456789012345678901234567890";
        
        assertThrows(RuntimeException.class, () -> 
            new Address(invalidCashAddress, pubKeyHash));
    }

    @Test
    void createAddress_WithInvalidPubKeyHashFormat_ShouldFail() {
        String cashAddress = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String invalidPubKeyHash = "invalid_hash"; // Too short and contains non-hex characters
        
        assertThrows(RuntimeException.class, () -> 
            new Address(cashAddress, invalidPubKeyHash));
    }

    @Test
    void createAddress_WithPubKeyHashTooShort_ShouldFail() {
        String cashAddress = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String shortPubKeyHash = "123456789"; // Too short (need 40 hex chars)
        
        assertThrows(RuntimeException.class, () -> 
            new Address(cashAddress, shortPubKeyHash));
    }

    @Test
    void createAddress_WithPubKeyHashTooLong_ShouldFail() {
        String cashAddress = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String longPubKeyHash = "12345678901234567890123456789012345678901"; // Too long (41 chars)
        
        assertThrows(RuntimeException.class, () -> 
            new Address(cashAddress, longPubKeyHash));
    }

    @Test
    void createAddress_WithPubKeyHashNonHexCharacters_ShouldFail() {
        String cashAddress = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String nonHexPubKeyHash = "123456789012345678901234567890123456789g"; // 'g' is not hex
        
        assertThrows(RuntimeException.class, () -> 
            new Address(cashAddress, nonHexPubKeyHash));
    }

    @Test
    void createCoinbaseAddress_ShouldWork() {
        Address coinbaseAddress = new Address("coinbase", "coinbase");
        
        assertEquals("coinbase", coinbaseAddress.getCashAddress());
        assertEquals("coinbase", coinbaseAddress.getPubKeyHash());
    }

    @Test
    void addressEquality_ShouldWorkCorrectly() {
        String cashAddress = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String pubKeyHash = "1234567890123456789012345678901234567890";
        
        Address address1 = new Address(cashAddress, pubKeyHash);
        Address address2 = new Address(cashAddress, pubKeyHash);
        
        assertEquals(address1, address2);
        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    void addressInequality_ShouldWorkCorrectly() {
        String cashAddress1 = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
        String cashAddress2 = "bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy";
        String pubKeyHash1 = "1234567890123456789012345678901234567890";
        String pubKeyHash2 = "0987654321098765432109876543210987654321";
        
        Address address1 = new Address(cashAddress1, pubKeyHash1);
        Address address2 = new Address(cashAddress2, pubKeyHash2);
        
        assertNotEquals(address1, address2);
    }
}