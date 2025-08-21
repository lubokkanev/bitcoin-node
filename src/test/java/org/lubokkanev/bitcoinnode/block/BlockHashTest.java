package org.lubokkanev.bitcoinnode.block;

import org.junit.jupiter.api.Test;
import org.lubokkanev.bitcoinnode.transaction.Address;
import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BlockHashTest {
    private Transaction buildNonCoinbaseTransaction(String txHash) {
        List<Xput> inputs = new ArrayList<>();
        inputs.add(new Xput(new Address("addr-in", ""), 100));
        List<Xput> outputs = new ArrayList<>();
        outputs.add(new Xput(new Address("addr-out", ""), 100));
        return new Transaction(inputs, outputs, txHash);
    }

    @Test
    public void sameContentProducesSameHash() throws Exception {
        Block genesis = new Block();

        Block b1 = new Block(genesis);
        Block b2 = new Block(genesis);

        assertArrayEquals(b1.getHash(), b2.getHash());
    }

    @Test
    public void changingNonceChangesHash() throws Exception {
        Block genesis = new Block();
        Block b = new Block(genesis);

        byte[] before = b.getHash();
        b.findNonce();
        byte[] after = b.getHash();

        assertFalse(Arrays.equals(before, after));
    }

    @Test
    public void differentTransactionsProduceDifferentHashes() throws Exception {
        Block genesis = new Block();

        Block b1 = new Block(genesis);
        b1.addTransaction(buildNonCoinbaseTransaction("tx-1"));

        Block b2 = new Block(genesis);
        b2.addTransaction(buildNonCoinbaseTransaction("tx-2"));

        assertFalse(Arrays.equals(b1.getHash(), b2.getHash()));
    }
}