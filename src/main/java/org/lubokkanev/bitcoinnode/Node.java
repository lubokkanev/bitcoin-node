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

    private final Mempool mempool = new Mempool();
    private final Set<Xput> utxos = new HashSet<>();
    private Block latestBlock;

    public Block getLatestBlock() {
        return latestBlock;
    }

    public Node(Block latestBlock) throws Exception {
        validateBlock(latestBlock);
        this.latestBlock = latestBlock;

        Set<Xput> allInputs = new HashSet<>();
        while (latestBlock != null) {
            for (Transaction tx : latestBlock.getTransactions()) {
                allInputs.addAll(tx.getInputs());

                for (Xput output : tx.getOutputs()) {
                    if (!allInputs.contains(output)) { // TODO: what about equal Xputs?
                       utxos.add(output);
                       log.trace("Added output {} to UTXO set.", output);
                    }
                }
            }
           log.trace("Parsed block {} hash: {}.", latestBlock.getNumber(), Arrays.toString(latestBlock.getHash()));

           latestBlock = latestBlock.getPrevious();
        }
    }

    public void receiveBlock(Block newBlock) throws Exception {
        try {
            validateBlock(newBlock);
            latestBlock = newBlock;
            log.trace("Received valid block {}", newBlock.getNumber());
        } catch (Exception e) {
            throw new Exception("Invalid block " + newBlock.getNumber() + ", hash: "
                  + Arrays.toString(newBlock.getHash()), e);
        }
    }

    public void validateBlock(Block newBlock) throws Exception { // TODO (improvement): create specialized exceptions
        if (!Objects.equals(newBlock.getPrevious(), latestBlock)) {
            throw new Exception("Invalid previous block."); // TODO: compare accumulated difficulty
        }

        if (latestBlock == null) {
            return; // nothing more to validate
        }

        if (newBlock.getDifficulty() < latestBlock.getDifficulty()) { // TODO: fix to use the DAA difficulty
            throw new Exception("Not sufficient difficulty.");
        }

        for (Transaction tx : newBlock.getTransactions()) {
            for (Xput in : tx.getInputs()) {
                if (!utxos.contains(in)) {
                    throw new Exception("Invalid transaction input: " + in + ".");
                }
            }
        }
    }

    public Block mineBlock() throws Exception {
        Block newBlock = new Block(latestBlock);
        newBlock.addTransactions(mempool.getTransactions());
        newBlock.findNonce(); // TODO: use the DAA to determine the difficulty
        receiveBlock(newBlock);
        log.info("Successfully mined block {}, hash: {}", newBlock.getNumber(), Arrays.toString(newBlock.getHash()));

        newBlock.propagateBlock();
        return newBlock;
    }

    public void receiveTransaction(Transaction transaction) throws Exception {
        try {
            validateTransaction(transaction);
            mempool.addTransaction(transaction);
            log.trace("Received valid transaction with hash: {}", transaction.getHash());
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
