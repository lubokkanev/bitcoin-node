package org.lubokkanev.bitcoinnode.mempool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lubokkanev.bitcoinnode.transaction.Address;
import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MempoolTest {

    private Mempool mempool;
    private Address testAddress1;
    private Address testAddress2;
    private Address coinbaseAddress;

    @BeforeEach
    void setUp() {
        mempool = new Mempool();
        testAddress1 = new Address("bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a", "1234567890123456789012345678901234567890");
        testAddress2 = new Address("bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy", "0987654321098765432109876543210987654321");
        coinbaseAddress = new Address("coinbase", "coinbase");
    }

    @Test
    void addValidTransaction_ShouldSucceed() throws Mempool.MempoolAcceptanceException {
        Xput input = new Xput(testAddress1, 100L);
        Xput output = new Xput(testAddress2, 90L);
        Transaction transaction = new Transaction(List.of(input), List.of(output), "txhash1");

        mempool.addTransaction(transaction);
        
        assertTrue(mempool.getTransactions().contains(transaction));
        assertEquals(1, mempool.getTransactions().size());
    }

    @Test
    void addCoinbaseTransaction_ShouldSucceed() throws Mempool.MempoolAcceptanceException {
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput output = new Xput(testAddress1, 50L);
        Transaction coinbaseTx = new Transaction(List.of(coinbaseInput), List.of(output), "coinbase_tx");

        mempool.addTransaction(coinbaseTx);
        
        assertTrue(mempool.getTransactions().contains(coinbaseTx));
    }

    @Test
    void addTransactionWithInsufficientInputs_ShouldFail() {
        Xput input = new Xput(testAddress1, 50L);
        Xput output = new Xput(testAddress2, 100L); // Output > Input
        Transaction transaction = new Transaction(List.of(input), List.of(output), "invalid_tx");

        assertThrows(Mempool.MempoolAcceptanceException.class, () -> 
            mempool.addTransaction(transaction));
    }

    @Test
    void addDuplicateTransaction_ShouldNotAddTwice() throws Mempool.MempoolAcceptanceException {
        Xput input = new Xput(testAddress1, 100L);
        Xput output = new Xput(testAddress2, 90L);
        Transaction transaction = new Transaction(List.of(input), List.of(output), "txhash1");

        mempool.addTransaction(transaction);
        mempool.addTransaction(transaction); // Try to add the same transaction again
        
        assertEquals(1, mempool.getTransactions().size());
    }

    @Test
    void addDoubleSpendingTransaction_ShouldFail() throws Mempool.MempoolAcceptanceException {
        Xput input = new Xput(testAddress1, 100L);
        Xput output1 = new Xput(testAddress2, 90L);
        Xput output2 = new Xput(coinbaseAddress, 90L);
        
        Transaction tx1 = new Transaction(List.of(input), List.of(output1), "tx1");
        Transaction tx2 = new Transaction(List.of(input), List.of(output2), "tx2"); // Same input, different output

        mempool.addTransaction(tx1);
        
        assertThrows(Mempool.MempoolAcceptanceException.class, () -> 
            mempool.addTransaction(tx2));
    }

    @Test
    void addTransactionWithNullInputs_ShouldFail() {
        // This test should fail at Transaction construction, not at mempool level
        assertThrows(RuntimeException.class, () -> 
            new Transaction(null, List.of(new Xput(testAddress1, 50L)), "invalid_tx"));
    }

    @Test
    void addTransactionWithEmptyInputs_ShouldFail() {
        // This test should fail at Transaction construction, not at mempool level
        assertThrows(RuntimeException.class, () -> 
            new Transaction(List.of(), List.of(new Xput(testAddress1, 50L)), "invalid_tx"));
    }
}