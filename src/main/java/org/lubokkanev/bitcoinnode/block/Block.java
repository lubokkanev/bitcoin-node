package org.lubokkanev.bitcoinnode.block;

import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    public void findNonce() {
        do {
            nonce++;
            blockHash = hash();
        } while (!checkDifficulty(getLeadingZeros()));
    }

    private boolean checkDifficulty(long leadingZeros) {
        return getDifficulty() >= leadingZeros;
    }

    private long getLeadingZeros() {
        // TODO: compute from the hash
        return 3;
    }

    public long getDifficulty() {
        // TODO: use the DAA
        return 3;
    }

    private byte[] hash() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            if (previousBlock != null) {
                messageDigest.update(previousBlock.getHash());
            }

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
            buffer.putLong(number);
            buffer.putLong(nonce);
            messageDigest.update(buffer.array());

            for (Transaction transaction : transactions) {
                String txHash = transaction.getHash();
                if (txHash != null) {
                    messageDigest.update(txHash.getBytes(StandardCharsets.UTF_8));
                }
            }

            return messageDigest.digest();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute block hash.", e);
        }
    }

    public void propagateBlock() {
        // TODO
    }

    public Block getPrevious() {
        return previousBlock;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }
}
