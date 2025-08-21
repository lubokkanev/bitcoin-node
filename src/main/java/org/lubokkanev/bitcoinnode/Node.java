package org.lubokkanev.bitcoinnode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lubokkanev.bitcoinnode.block.Block;
import org.lubokkanev.bitcoinnode.mempool.Mempool;
import org.lubokkanev.bitcoinnode.transaction.Xput;
import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.exception.InvalidBlockException;
import org.lubokkanev.bitcoinnode.exception.InvalidTransactionException;
import org.lubokkanev.bitcoinnode.exception.InsufficientDifficultyException;

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

    public Node(Block latestBlock) throws InvalidBlockException {
        validateBlock(latestBlock);
        this.latestBlock = latestBlock;

        Set<Xput> allInputs = new HashSet<>();
        Block currentBlock = latestBlock;
        while (currentBlock != null) {
            for (Transaction tx : currentBlock.getTransactions()) {
                allInputs.addAll(tx.getInputs());

                for (Xput output : tx.getOutputs()) {
                    // Check if this output has been consumed by any input in the chain
                    boolean isConsumed = allInputs.stream()
                        .anyMatch(input -> input.getAddress().equals(output.getAddress()) && 
                                          input.getAmountSats() == output.getAmountSats());
                    if (!isConsumed) {
                       utxos.add(output);
                       log.trace("Added output {} to UTXO set.", output);
                    }
                }
            }
           log.trace("Parsed block {} hash: {}.", currentBlock.getNumber(), Arrays.toString(currentBlock.getHash()));

           currentBlock = currentBlock.getPrevious();
        }
    }

    public void receiveBlock(Block newBlock) throws InvalidBlockException {
        try {
            validateBlock(newBlock);
            latestBlock = newBlock;
            log.trace("Received valid block {}", newBlock.getNumber());
        } catch (InvalidBlockException e) {
            throw new InvalidBlockException("Invalid block " + newBlock.getNumber() + ", hash: "
                  + Arrays.toString(newBlock.getHash()), e);
        }
    }

    public void validateBlock(Block newBlock) throws InvalidBlockException {
        // Check if the previous block reference is correct
        if (!Objects.equals(newBlock.getPrevious(), latestBlock)) {
            // For genesis block (latestBlock is null), this is valid
            if (latestBlock != null) {
                // Compare accumulated difficulty by checking if the new block builds on the most difficult chain
                long newBlockTotalDifficulty = calculateAccumulatedDifficulty(newBlock);
                long currentChainDifficulty = calculateAccumulatedDifficulty(latestBlock);
                
                if (newBlockTotalDifficulty <= currentChainDifficulty) {
                    throw new InvalidBlockException("Block does not build on the most difficult chain.");
                }
            }
        }

        if (latestBlock == null) {
            return; // genesis block - nothing more to validate
        }

        // Use the block's built-in DAA difficulty calculation
        long expectedDifficulty = newBlock.getDifficulty();
        if (newBlock.getLeadingZeros() < expectedDifficulty) {
            throw new InsufficientDifficultyException("Block does not meet required difficulty: " + expectedDifficulty);
        }

        // Validate all transactions in the block
        for (Transaction tx : newBlock.getTransactions()) {
            validateTransactionInputs(tx);
        }
    }

    public Block mineBlock() throws InvalidBlockException {
        Block newBlock = new Block(latestBlock);
        newBlock.addTransactions(mempool.getTransactions());
        // The Block class already implements DAA in its getDifficulty() method
        // findNonce() will use this difficulty automatically
        newBlock.findNonce();
        receiveBlock(newBlock);
        log.info("Successfully mined block {}, hash: {}", newBlock.getNumber(), Arrays.toString(newBlock.getHash()));

        newBlock.propagateBlock();
        return newBlock;
    }

    public void receiveTransaction(Transaction transaction) throws InvalidTransactionException {
        try {
            validateTransaction(transaction);
            mempool.addTransaction(transaction);
            log.trace("Received valid transaction with hash: {}", transaction.getHash());
        } catch (Exception e) {
            throw new InvalidTransactionException("Invalid transaction.", e);
        }
    }

    private void validateTransaction(Transaction transaction) throws InvalidTransactionException {
        if (!utxos.containsAll(transaction.getInputs())) {
            throw new InvalidTransactionException("Transaction contains invalid inputs.");
        }
    }

    private void validateTransactionInputs(Transaction transaction) throws InvalidBlockException {
        for (Xput input : transaction.getInputs()) {
            if (!input.isCoinbase() && !utxos.contains(input)) {
                throw new InvalidBlockException("Invalid transaction input: " + input + ".");
            }
        }
    }

    private long calculateAccumulatedDifficulty(Block block) {
        long totalDifficulty = 0;
        Block current = block;
        while (current != null) {
            totalDifficulty += current.getDifficulty();
            current = current.getPrevious();
        }
        return totalDifficulty;
    }

    public Set<Transaction> getUnconfirmedTransactions() {
        return mempool.getTransactions();
    }
}
