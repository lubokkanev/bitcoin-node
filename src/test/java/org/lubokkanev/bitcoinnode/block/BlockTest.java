package org.lubokkanev.bitcoinnode.block;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lubokkanev.bitcoinnode.Constants;
import org.lubokkanev.bitcoinnode.transaction.Address;
import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    private Address testAddress1;
    private Address testAddress2;
    private Address coinbaseAddress;

    @BeforeEach
    void setUp() {
        testAddress1 = new Address("bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a", "1234567890123456789012345678901234567890");
        testAddress2 = new Address("bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy", "0987654321098765432109876543210987654321");
        coinbaseAddress = new Address("coinbase", "coinbase");
    }

    @Test
    void createGenesisBlock_ShouldHaveCorrectNumber() {
        Block genesisBlock = new Block();
        assertEquals(1, genesisBlock.getNumber());
        assertNull(genesisBlock.getPrevious());
    }

    @Test
    void createChildBlock_ShouldHaveCorrectNumberAndParent() {
        Block genesisBlock = new Block();
        Block childBlock = new Block(genesisBlock);
        
        assertEquals(2, childBlock.getNumber());
        assertEquals(genesisBlock, childBlock.getPrevious());
    }

    @Test
    void addValidTransaction_ShouldSucceed() throws Exception {
        Block block = new Block();
        Xput input = new Xput(testAddress1, 100L);
        Xput output = new Xput(testAddress2, 90L);
        Transaction tx = new Transaction(List.of(input), List.of(output), "tx1");

        block.addTransaction(tx);
        
        assertTrue(block.getTransactions().contains(tx));
        assertEquals(1, block.getTransactions().size());
    }

    @Test
    void addCoinbaseTransaction_ShouldSucceed() throws Exception {
        Block block = new Block();
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput output = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Transaction coinbaseTx = new Transaction(List.of(coinbaseInput), List.of(output), "coinbase_tx");

        block.addTransaction(coinbaseTx);
        
        assertTrue(block.getTransactions().contains(coinbaseTx));
    }

    @Test
    void addMultipleCoinbaseTransactions_ShouldFail() throws Exception {
        Block block = new Block();
        
        Xput coinbaseInput1 = new Xput(coinbaseAddress, 0L);
        Xput output1 = new Xput(testAddress1, 25L * Constants.BITCOIN);
        Transaction coinbaseTx1 = new Transaction(List.of(coinbaseInput1), List.of(output1), "coinbase_tx1");

        Xput coinbaseInput2 = new Xput(coinbaseAddress, 0L);
        Xput output2 = new Xput(testAddress2, 25L * Constants.BITCOIN);
        Transaction coinbaseTx2 = new Transaction(List.of(coinbaseInput2), List.of(output2), "coinbase_tx2");

        block.addTransaction(coinbaseTx1);
        
        assertThrows(Exception.class, () -> block.addTransaction(coinbaseTx2));
    }

    @Test
    void addInvalidCoinbaseTransactionWithMultipleInputs_ShouldFail() {
        Block block = new Block();
        
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput regularInput = new Xput(testAddress1, 100L);
        Xput output = new Xput(testAddress2, 50L);
        Transaction invalidCoinbaseTx = new Transaction(
            List.of(coinbaseInput, regularInput), List.of(output), "invalid_coinbase");

        assertThrows(Exception.class, () -> block.addTransaction(invalidCoinbaseTx));
    }

    @Test
    void getDifficulty_ForGenesisBlock_ShouldReturnBaseDifficulty() {
        Block genesisBlock = new Block();
        assertEquals(Constants.BASE_DIFFICULTY, genesisBlock.getDifficulty());
    }

    @Test
    void getDifficulty_ForNonRetargetBlock_ShouldReturnPreviousDifficulty() {
        Block genesisBlock = new Block();
        Block block2 = new Block(genesisBlock);
        
        // Block 2 is not at a retarget point (every 10 blocks), so should use previous difficulty
        assertEquals(Constants.BASE_DIFFICULTY, block2.getDifficulty());
    }

    @Test
    void findNonce_ShouldSetValidNonceAndHash() throws Exception {
        Block block = new Block();
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput output = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Transaction coinbaseTx = new Transaction(List.of(coinbaseInput), List.of(output), "coinbase_tx");
        
        block.addTransaction(coinbaseTx);
        block.findNonce();
        
        assertNotNull(block.getHash());
        assertTrue(block.getLeadingZeros() >= block.getDifficulty());
        assertTrue(block.getTimestampSec() > 0);
    }

    @Test
    void getHash_ShouldReturnConsistentHash() throws Exception {
        Block block = new Block();
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput output = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Transaction coinbaseTx = new Transaction(List.of(coinbaseInput), List.of(output), "coinbase_tx");
        
        block.addTransaction(coinbaseTx);
        
        byte[] hash1 = block.getHash();
        byte[] hash2 = block.getHash();
        
        assertArrayEquals(hash1, hash2);
    }

    @Test
    void propagateBlock_ShouldCompleteWithoutError() {
        Block block = new Block();
        
        // This should not throw an exception
        assertDoesNotThrow(() -> block.propagateBlock());
    }

    @Test
    void getLeadingZeros_ShouldReturnCorrectCount() throws Exception {
        Block block = new Block();
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput output = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Transaction coinbaseTx = new Transaction(List.of(coinbaseInput), List.of(output), "coinbase_tx");
        
        block.addTransaction(coinbaseTx);
        block.findNonce(); // This will find a nonce that satisfies the difficulty
        
        long leadingZeros = block.getLeadingZeros();
        assertTrue(leadingZeros >= Constants.BASE_DIFFICULTY);
    }

    @Test
    void setTimestamp_ShouldUpdateTimestamp() {
        Block block = new Block();
        long testTimestamp = 1640995200L; // Jan 1, 2022
        
        block.setTimestampSec(testTimestamp);
        
        assertEquals(testTimestamp, block.getTimestampSec());
    }
}