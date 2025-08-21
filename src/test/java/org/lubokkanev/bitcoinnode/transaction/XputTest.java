package org.lubokkanev.bitcoinnode.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lubokkanev.bitcoinnode.Constants;

import static org.junit.jupiter.api.Assertions.*;

class XputTest {

    private Address regularAddress;
    private Address coinbaseAddress;

    @BeforeEach
    void setUp() {
        regularAddress = new Address("bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a", "1234567890123456789012345678901234567890");
        coinbaseAddress = new Address("coinbase", "coinbase");
    }

    @Test
    void createXput_ShouldSetCorrectValues() {
        long amount = 100L * Constants.BITCOIN;
        Xput xput = new Xput(regularAddress, amount);

        assertEquals(regularAddress, xput.getAddress());
        assertEquals(amount, xput.getAmountSats());
    }

    @Test
    void isCoinbase_WithCoinbaseAddress_ShouldReturnTrue() {
        Xput coinbaseXput = new Xput(coinbaseAddress, 0L);

        assertTrue(coinbaseXput.isCoinbase());
    }

    @Test
    void isCoinbase_WithRegularAddress_ShouldReturnFalse() {
        Xput regularXput = new Xput(regularAddress, 100L);

        assertFalse(regularXput.isCoinbase());
    }

    @Test
    void isCoinbase_WithCashAddressMatching_ShouldReturnTrue() {
        Address addressWithCoinbaseCashAddr = new Address(Constants.COINBASE_ADDRESS, "1234567890123456789012345678901234567890");
        Xput xput = new Xput(addressWithCoinbaseCashAddr, 0L);

        assertTrue(xput.isCoinbase());
    }

    @Test
    void isCoinbase_WithPubKeyHashMatching_ShouldReturnTrue() {
        Address addressWithCoinbasePubKeyHash = new Address("bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy", Constants.COINBASE_ADDRESS);
        Xput xput = new Xput(addressWithCoinbasePubKeyHash, 0L);

        assertTrue(xput.isCoinbase());
    }

    @Test
    void equals_WithSameAddressAndAmount_ShouldReturnTrue() {
        long amount = 50L * Constants.BITCOIN;
        Xput xput1 = new Xput(regularAddress, amount);
        Xput xput2 = new Xput(regularAddress, amount);

        assertEquals(xput1, xput2);
        assertEquals(xput1.hashCode(), xput2.hashCode());
    }

    @Test
    void equals_WithDifferentAddress_ShouldReturnFalse() {
        long amount = 50L * Constants.BITCOIN;
        Xput xput1 = new Xput(regularAddress, amount);
        Xput xput2 = new Xput(coinbaseAddress, amount);

        assertNotEquals(xput1, xput2);
    }

    @Test
    void equals_WithDifferentAmount_ShouldReturnFalse() {
        Xput xput1 = new Xput(regularAddress, 50L * Constants.BITCOIN);
        Xput xput2 = new Xput(regularAddress, 100L * Constants.BITCOIN);

        assertNotEquals(xput1, xput2);
    }

    @Test
    void equals_WithNull_ShouldReturnFalse() {
        Xput xput = new Xput(regularAddress, 50L * Constants.BITCOIN);

        assertNotEquals(xput, null);
    }

    @Test
    void equals_WithDifferentClass_ShouldReturnFalse() {
        Xput xput = new Xput(regularAddress, 50L * Constants.BITCOIN);
        String notAnXput = "not an xput";

        assertNotEquals(xput, notAnXput);
    }

    @Test
    void getAmountSats_ShouldReturnCorrectAmount() {
        long amount = 123456789L;
        Xput xput = new Xput(regularAddress, amount);

        assertEquals(amount, xput.getAmountSats());
    }

    @Test
    void getAddress_ShouldReturnCorrectAddress() {
        Xput xput = new Xput(regularAddress, 1000L);

        assertEquals(regularAddress, xput.getAddress());
    }
}