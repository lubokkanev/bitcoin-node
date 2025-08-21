package org.lubokkanev.bitcoinnode.block;

import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;
import static org.lubokkanev.bitcoinnode.Constants.BASE_DIFFICULTY;
import static org.lubokkanev.bitcoinnode.Constants.DIFFICULTY_ADJUSTMENT_INTERVAL;
import static org.lubokkanev.bitcoinnode.Constants.TARGET_BLOCK_TIME_SEC;
import static org.lubokkanev.bitcoinnode.Constants.DAA_MIN_ADJUSTMENT;
import static org.lubokkanev.bitcoinnode.Constants.DAA_MAX_ADJUSTMENT;
import static org.lubokkanev.bitcoinnode.Constants.MIN_DIFFICULTY_BITS;
import static org.lubokkanev.bitcoinnode.Constants.MAX_DIFFICULTY_BITS;
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
    private long timestampSec;

    public Block(Block previousBlock) {
        this.previousBlock = previousBlock;
        this.number = previousBlock.getNumber() + 1;
    }

    public Block() {
        previousBlock = null;
        number = 1;
        timestampSec = System.currentTimeMillis() / 1000L;
    }

    public long getNumber() {
        return number;
    }

    public long getTimestampSec() {
        return timestampSec;
    }

    public void setTimestampSec(long timestampSec) {
        this.timestampSec = timestampSec;
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

        if (timestampSec == 0) {
            timestampSec = System.currentTimeMillis() / 1000L;
        }
    }

    private boolean checkDifficulty(long leadingZeros) {
        return leadingZeros >= getDifficulty();
    }

    private long getLeadingZeros() {
        byte[] hashBytes = getHash();
        long count = 0;

        for (byte b : hashBytes) {
            int unsigned = b & 0xFF;
            if (unsigned == 0) {
                count += 8;
            } else {
                // Count leading zero bits in this byte
                count += Integer.numberOfLeadingZeros(unsigned) - 24; // 32-bit result -> 8-bit adjustment
                break;
            }
        }

        return count;
    }

    public long getDifficulty() {
        // Discrete retarget: every DIFFICULTY_ADJUSTMENT_INTERVAL blocks, adjust based on elapsed time
        if (previousBlock == null) {
            return BASE_DIFFICULTY;
        }

        long heightIndex = Math.max(0, number - 1);
        boolean isRetargetPoint = (heightIndex % DIFFICULTY_ADJUSTMENT_INTERVAL) == 0;
        if (!isRetargetPoint) {
            return previousBlock.getDifficulty();
        }

        // Walk back interval-1 blocks to find the first block in the window
        Block windowStart = this;
        for (int i = 0; i < DIFFICULTY_ADJUSTMENT_INTERVAL - 1; i++) {
            if (windowStart.previousBlock == null) {
                break;
            }
            windowStart = windowStart.previousBlock;
        }

        long windowEndTime = previousBlock.getTimestampSec();
        long windowStartTime = windowStart.getTimestampSec();
        if (windowStartTime == 0 || windowEndTime == 0) {
            return previousBlock.getDifficulty();
        }

        long actualTimespan = Math.max(1, windowEndTime - windowStartTime);
        long desiredTimespan = DIFFICULTY_ADJUSTMENT_INTERVAL * TARGET_BLOCK_TIME_SEC;

        double factor = (double) actualTimespan / (double) desiredTimespan;
        factor = Math.max(DAA_MIN_ADJUSTMENT, Math.min(DAA_MAX_ADJUSTMENT, factor));

        // Map factor to bit change: deltaBits ≈ round(log2(factor))
        double deltaBitsDouble = Math.log(factor) / Math.log(2.0);
        long deltaBits = Math.round(deltaBitsDouble);

        long newBits = previousBlock.getDifficulty() - deltaBits;
        if (newBits < MIN_DIFFICULTY_BITS) newBits = MIN_DIFFICULTY_BITS;
        if (newBits > MAX_DIFFICULTY_BITS) newBits = MAX_DIFFICULTY_BITS;
        return newBits;
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
