package org.lubokkanev.bitcoinnode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lubokkanev.bitcoinnode.block.Block;
import org.lubokkanev.bitcoinnode.transaction.Address;
import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.lubokkanev.bitcoinnode.Constants.BITCOIN;
import static org.lubokkanev.bitcoinnode.Constants.COINBASE_ADDRESS;

public class NodeTest {
    private final String AVAILABLE_INPUT = "available-input";
    private final String UNAVAILABLE_INPUT = "unavailable-input";
    private Node node;

    @BeforeEach
    public void setup() throws Exception {
        List<Xput> inputs = new ArrayList<>();
        inputs.add(new Xput(new Address(COINBASE_ADDRESS, ""), 50 * BITCOIN));
        List<Xput> outputs = new ArrayList<>();
        outputs.add(new Xput(new Address(AVAILABLE_INPUT, ""), 50 * BITCOIN));
        Transaction initialTransaction = new Transaction(inputs, outputs, "");

        Block initialBlock = new Block();
        initialBlock.addTransaction(initialTransaction);
        node = new Node(initialBlock);
    }

    @Test
    public void receiveValidTransaction() {
        List<Xput> inputs = new ArrayList<>();
        inputs.add(new Xput(new Address(AVAILABLE_INPUT, ""), 50 * BITCOIN));
        List<Xput> outputs = new ArrayList<>();
        outputs.add(new Xput(new Address("test-output", ""), 50 * BITCOIN));
        Transaction validTransaction = new Transaction(inputs, outputs, "");

        assertThrows(Exception.class, () -> node.receiveTransaction(validTransaction));
    }

    @Test
    public void receiveInvalidTransaction() {
        List<Xput> inputs = new ArrayList<>();
        inputs.add(new Xput(new Address(UNAVAILABLE_INPUT, ""), 50 * BITCOIN));
        List<Xput> outputs = new ArrayList<>();
        outputs.add(new Xput(new Address("test-output", ""), 50 * BITCOIN));
        Transaction invalidTransaction = new Transaction(inputs, outputs, "");

        assertThrows(Exception.class, () -> node.receiveTransaction(invalidTransaction));
    }

    @Test
    public void receiveValidBlock() throws Exception {
        Block validBlock = new Block(node.getLatestBlock());
        node.receiveBlock(validBlock);
        assertArrayEquals(node.getLatestBlock().getHash(), validBlock.getHash());
    }

    @Test
    public void receiveInvalidBlock() {
        Block invalidBlock = new Block();
        assertThrows(Exception.class, () -> node.receiveBlock(invalidBlock));
    }

    @Test
    public void mineBlock() {
        Block minedBlock = node.mineBlock();
        assertDoesNotThrow(() -> node.validateBlock(minedBlock));
    }

    @Test
    public void multipleCoinbaseInputs() {
        List<Xput> inputs = new ArrayList<>();
        inputs.add(new Xput(new Address(COINBASE_ADDRESS, ""), 50 * BITCOIN));
        List<Xput> outputs = new ArrayList<>();
        outputs.add(new Xput(new Address(AVAILABLE_INPUT, ""), 50 * BITCOIN));
        Transaction coinbaseTransaction = new Transaction(inputs, outputs, "");

        assertThrows(Exception.class, () -> node.getLatestBlock().addTransaction(coinbaseTransaction));
    }

    @Test
    public void utxos() {
        // add multiple blocks reusing Xputs
        // verify the utxo is as expected
    }
}
