package org.lubokkanev.bitcoinnode.mempool;

import org.lubokkanev.bitcoinnode.transaction.Transaction;
import org.lubokkanev.bitcoinnode.transaction.Xput;

import java.util.HashSet;
import java.util.Set;

public class Mempool {
    private Set<Transaction> unconfirmedTransactions = new HashSet<>();

    public void addTransaction(Transaction transaction) throws MempoolAcceptanceException {
        if (!unconfirmedTransactions.contains(transaction)) {
            if (isValid(transaction)) {
                unconfirmedTransactions.add(transaction);
            } else {
                throw new MempoolAcceptanceException("Invalid transaction.");
            }
        }
    }

    private boolean isValid(Transaction transaction) {
        // Validate transaction structure
        if (transaction.getInputs() == null || transaction.getInputs().isEmpty() ||
            transaction.getOutputs() == null || transaction.getOutputs().isEmpty()) {
            return false;
        }
        
        // Check for double spending within the mempool
        for (Xput input : transaction.getInputs()) {
            for (Transaction existingTx : unconfirmedTransactions) {
                if (existingTx.getInputs().contains(input)) {
                    return false; // Double spending detected
                }
            }
        }
        
        // Validate input/output balance (simplified - inputs should cover outputs)
        long totalInputs = transaction.getInputs().stream()
            .filter(input -> !input.isCoinbase())
            .mapToLong(Xput::getAmountSats)
            .sum();
        long totalOutputs = transaction.getOutputs().stream()
            .mapToLong(Xput::getAmountSats)
            .sum();
            
        // For non-coinbase transactions, inputs must be >= outputs (allowing for fees)
        boolean isCoinbaseTransaction = transaction.getInputs().stream().anyMatch(Xput::isCoinbase);
        if (!isCoinbaseTransaction && totalInputs < totalOutputs) {
            return false;
        }
        
        return true;
    }

    public Set<Transaction> getTransactions() {
        return unconfirmedTransactions;
    }

    public static class MempoolAcceptanceException extends Exception {
        public MempoolAcceptanceException(String message) {
            super(message);
        }
    }
}
