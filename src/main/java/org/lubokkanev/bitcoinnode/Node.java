package org.lubokkanev.bitcoinnode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lubokkanev.bitcoinnode.block.Block;
import org.lubokkanev.bitcoinnode.mempool.Mempool;
import org.lubokkanev.bitcoinnode.transaction.Xput;
import org.lubokkanev.bitcoinnode.transaction.Transaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Node {
    private static final Logger log = LoggerFactory.getLogger(Node.class);

    private Mempool mempool = new Mempool();

    public Block getLatestBlock() {
        return latestBlock;
    }

    private Block latestBlock;
    private Set<Xput> utxos = new HashSet<>();

    public Node(Block latestBlock) {
        this.latestBlock = latestBlock;

        Set<Xput> inputs = new HashSet<>();
        while (latestBlock != null) {
            for (Transaction t : latestBlock.getTransactions()) {
                inputs.addAll(t.getInputs());

                for (Xput output : t.getOutputs()) {
                    if (!inputs.contains(output)) { // TODO: what about equal Xputs?
                        utxos.add(output);
                        log.trace("Added output " + output + " to UTXO set.");
                    }
                }
            }
            log.trace("Parsed block " + latestBlock.getNumber() + " hash: " + Arrays.toString(latestBlock.getHash())
                  + ".");

            latestBlock = latestBlock.getPrevious();
        }
    }

    public void receiveBlock(Block newBlock) throws Exception {
        try {
            validateBlock(newBlock);
            latestBlock = newBlock;
            log.trace("Received valid block " + newBlock.getNumber());
        } catch (Exception e) {
            throw new Exception("Invalid block " + newBlock.getNumber() + ", hash: "
                  + Arrays.toString(newBlock.getHash()), e);
        }
    }

    public void validateBlock(Block newBlock) throws Exception { // TODO (improvement): create specialized exceptions
        if (!Objects.equals(newBlock.getPrevious(), latestBlock)) {
            throw new Exception("Invalid previous block."); // TODO: compare accumulated difficulty
        }

        for (Transaction t : newBlock.getTransactions()) {
            for (Xput i : t.getInputs()) {
                if (!utxos.contains(i)) {
                    throw new Exception("Invalid transaction input: " + i + ".");
                }
            }
        }

        if (newBlock.getDifficulty() < latestBlock.getDifficulty()) { // TODO: fix to use the DAA difficulty
            throw new Exception("Not sufficient difficulty.");
        }
    }

    public Block mineBlock() {
        Block newBlock = new Block(latestBlock);
        newBlock.addTransactions(mempool.getTransactions());
        newBlock.findNonce(2);
        newBlock.propagateBlock();
        log.info("Successfully mined block " + newBlock.getNumber() + ", hash: " + Arrays.toString(newBlock.getHash()));
        return newBlock;
    }

    public void receiveTransaction(Transaction transaction) throws Exception {
        try {
            validateTransaction(transaction);
            mempool.addTransaction(transaction);
            log.trace("Received valid transaction with hash: " + transaction.getHash());
        } catch (Exception e) {
            throw new Exception("Invalid transaction.", e);
        }
    }

    private void validateTransaction(Transaction transaction) throws Exception {
        if (!utxos.containsAll(transaction.getInputs())) {
            throw new Exception("Invalid inputs.");
        }
    }

    public Set<Transaction> getUnconfirmedTransactions() {
        return mempool.getTransactions();
    }
}
