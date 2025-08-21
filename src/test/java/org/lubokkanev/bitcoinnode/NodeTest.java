package org.lubokkanev.bitcoinnode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lubokkanev.bitcoinnode.block.Block;
import org.lubokkanev.bitcoinnode.exception.InvalidBlockException;
import org.lubokkanev.bitcoinnode.exception.InvalidTransactionException;
import org.lubokkanev.bitcoinnode.transaction.Address;
import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    private Node node;
    private Block genesisBlock;
    private Address testAddress1;
    private Address testAddress2;
    private Address coinbaseAddress;

    @BeforeEach
    void setUp() throws Exception {
        testAddress1 = new Address("bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a", "1234567890123456789012345678901234567890");
        testAddress2 = new Address("bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy", "0987654321098765432109876543210987654321");
        coinbaseAddress = new Address("coinbase", "coinbase");

        // Create genesis block with coinbase transaction
        genesisBlock = new Block();
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput genesisOutput = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Transaction genesisTx = new Transaction(List.of(coinbaseInput), List.of(genesisOutput), "genesis_tx");
        
        genesisBlock.addTransaction(genesisTx);
        genesisBlock.setTimestampSec(System.currentTimeMillis() / 1000L);
        genesisBlock.findNonce();
        
        node = new Node(genesisBlock);
    }

    @Test
    void createNodeWithValidGenesisBlock_ShouldSucceed() {
        assertNotNull(node);
        assertEquals(genesisBlock, node.getLatestBlock());
    }

    @Test
    void receiveValidBlock_ShouldSucceed() throws Exception {
        Block newBlock = new Block(genesisBlock);
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput blockReward = new Xput(testAddress2, 25L * Constants.BITCOIN);
        Transaction coinbaseTx = new Transaction(List.of(coinbaseInput), List.of(blockReward), "block2_coinbase");
        
        newBlock.addTransaction(coinbaseTx);
        newBlock.findNonce();

        node.receiveBlock(newBlock);
        
        assertEquals(newBlock, node.getLatestBlock());
        assertEquals(2, node.getLatestBlock().getNumber());
    }

    @Test
    void receiveBlockWithInvalidPrevious_ShouldFail() throws Exception {
        Block invalidBlock = new Block(); // New genesis block, not building on current chain
        invalidBlock.setTimestampSec(System.currentTimeMillis() / 1000L);
        
        assertThrows(InvalidBlockException.class, () -> 
            node.receiveBlock(invalidBlock));
    }

    @Test
    void receiveValidTransaction_ShouldSucceed() throws Exception {
        // Use UTXO from genesis block
        Xput input = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Xput output1 = new Xput(testAddress2, 25L * Constants.BITCOIN);
        Xput output2 = new Xput(testAddress1, 24L * Constants.BITCOIN); // Change, leaving 1 BTC as fee
        Transaction tx = new Transaction(List.of(input), List.of(output1, output2), "tx1");

        node.receiveTransaction(tx);
        
        assertTrue(node.getUnconfirmedTransactions().contains(tx));
    }

    @Test
    void receiveTransactionWithInvalidInputs_ShouldFail() {
        // Try to spend non-existent UTXO
        Xput invalidInput = new Xput(testAddress2, 100L * Constants.BITCOIN);
        Xput output = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Transaction invalidTx = new Transaction(List.of(invalidInput), List.of(output), "invalid_tx");

        assertThrows(InvalidTransactionException.class, () -> 
            node.receiveTransaction(invalidTx));
    }

    @Test
    void mineBlock_ShouldSucceed() throws Exception {
        // Add a transaction to mempool first
        Xput input = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Xput output = new Xput(testAddress2, 49L * Constants.BITCOIN);
        Transaction tx = new Transaction(List.of(input), List.of(output), "tx1");
        
        node.receiveTransaction(tx);
        
        Block minedBlock = node.mineBlock();
        
        assertNotNull(minedBlock);
        assertEquals(2, minedBlock.getNumber());
        assertTrue(minedBlock.getTransactions().contains(tx));
        assertEquals(minedBlock, node.getLatestBlock());
    }

    @Test
    void validateBlockWithInsufficientDifficulty_ShouldFail() throws Exception {
        Block newBlock = new Block(genesisBlock) {
            @Override
            public long getLeadingZeros() {
                return 0; // Insufficient difficulty
            }
        };
        
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput blockReward = new Xput(testAddress2, 25L * Constants.BITCOIN);
        Transaction coinbaseTx = new Transaction(List.of(coinbaseInput), List.of(blockReward), "block2_coinbase");
        newBlock.addTransaction(coinbaseTx);

        assertThrows(InvalidBlockException.class, () -> 
            node.receiveBlock(newBlock));
    }

    @Test
    void getUnconfirmedTransactions_ShouldReturnMempoolTransactions() throws Exception {
        Xput input = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Xput output = new Xput(testAddress2, 49L * Constants.BITCOIN);
        Transaction tx = new Transaction(List.of(input), List.of(output), "tx1");
        
        node.receiveTransaction(tx);
        Set<Transaction> unconfirmedTxs = node.getUnconfirmedTransactions();
        
        assertEquals(1, unconfirmedTxs.size());
        assertTrue(unconfirmedTxs.contains(tx));
    }
}