package org.lubokkanev.bitcoinnode.block;

import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;

import java.util.HashSet;
import java.util.Set;

public class Block {
    private Set<Transaction> transactions = new HashSet<>();
    private long nonce = 0;
    private Block previousBlock;
    private byte[] blockHash;
    private long number;

    public Block(Block previousBlock) {
        this.previousBlock = previousBlock;
        this.number = previousBlock.getNumber() + 1;
    }

    public Block() {
        previousBlock = null;
        number = 1;
    }

    public long getNumber() {
        return number;
    }

    public void addTransaction(Transaction transaction) throws Exception {
        try {
            validate(transaction);
            transactions.add(transaction);
        } catch (Exception e) {
            throw new Exception("Invalid transaction.", e);
        }
    }

    private void validate(Transaction transaction) throws Exception {
        for (Xput input : transaction.getInputs()) {
            if (input.isCoinbase()) {
                if (theresAlreadyCoinbaseInput()) {
                    throw new Exception("This block already contains a coinbase input, you can't add another one."); // TODO: I'm pretty sure there can be multiple coinbase inputs. Probably the transaction can be coinbase or not.
                }
            }
        }
    }

    private boolean theresAlreadyCoinbaseInput() {
        for (Transaction t : transactions) {
            for (Xput input : t.getInputs()) {
                if (input.isCoinbase()) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addTransactions(Set<Transaction> transactions) {
        this.transactions.addAll(transactions);
    }

    public byte[] getHash() {
        if (blockHash == null) {
            blockHash = hash();
        }

        return blockHash;
    }

    public void findNonce(long difficultyZeros) {
        do {
            nonce++;
            blockHash = hash();
        } while (!checkDifficulty(difficultyZeros));
    }

    private boolean checkDifficulty(long difficultyZeros) {
        return getDifficulty() >= difficultyZeros;
    }

    public long getDifficulty() {
        // TODO
        return 3;
    }

    private byte[] hash() {
        // TODO
        // use nonce, transactions and previous block
        return new byte[0];
    }

    public void propagateBlock() {
        // TODO
        // also add it yourself?
    }

    public Block getPrevious() {
        return previousBlock;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }
}
