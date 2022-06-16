package org.lubokkanev.bitcoinnode.mempool;

import org.lubokkanev.bitcoinnode.transaction.Transaction;

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
        // TODO: the transactions are already validated at latest-block level, but unconfirmed chain validation is also needed
        return true;
    }

    public Set<Transaction> getTransactions() {
        return unconfirmedTransactions;
    }

    public static class MempoolAcceptanceException extends Exception {
        public MempoolAcceptanceException(String invalid_transaction) {
        }
    }
}
