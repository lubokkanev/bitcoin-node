package org.lubokkanev.bitcoinnode.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lubokkanev.bitcoinnode.Constants;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private Address testAddress1;
    private Address testAddress2;
    private Address coinbaseAddress;

    @BeforeEach
    void setUp() {
        testAddress1 = new Address("bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a", "1234567890123456789012345678901234567890");
        testAddress2 = new Address("bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy", "0987654321098765432109876543210987654321");
        coinbaseAddress = new Address("coinbase", "coinbase");
    }

    @Test
    void createValidTransaction_ShouldSucceed() {
        Xput input = new Xput(testAddress1, 100L * Constants.BITCOIN);
        Xput output = new Xput(testAddress2, 90L * Constants.BITCOIN);
        String txHash = "tx123";

        Transaction transaction = new Transaction(List.of(input), List.of(output), txHash);

        assertEquals(List.of(input), transaction.getInputs());
        assertEquals(List.of(output), transaction.getOutputs());
        assertEquals(txHash, transaction.getHash());
    }

    @Test
    void createCoinbaseTransaction_ShouldSucceed() {
        Xput coinbaseInput = new Xput(coinbaseAddress, 0L);
        Xput blockReward = new Xput(testAddress1, 50L * Constants.BITCOIN);
        String coinbaseTxHash = "coinbase_tx_123";

        Transaction coinbaseTransaction = new Transaction(List.of(coinbaseInput), List.of(blockReward), coinbaseTxHash);

        assertEquals(List.of(coinbaseInput), coinbaseTransaction.getInputs());
        assertEquals(List.of(blockReward), coinbaseTransaction.getOutputs());
        assertEquals(coinbaseTxHash, coinbaseTransaction.getHash());
    }

    @Test
    void createTransactionWithMultipleInputsAndOutputs_ShouldSucceed() {
        Xput input1 = new Xput(testAddress1, 50L * Constants.BITCOIN);
        Xput input2 = new Xput(testAddress2, 30L * Constants.BITCOIN);
        Xput output1 = new Xput(testAddress1, 40L * Constants.BITCOIN);
        Xput output2 = new Xput(testAddress2, 39L * Constants.BITCOIN); // 1 BTC fee

        Transaction transaction = new Transaction(
            List.of(input1, input2), 
            List.of(output1, output2), 
            "multi_tx_123"
        );

        assertEquals(2, transaction.getInputs().size());
        assertEquals(2, transaction.getOutputs().size());
        assertTrue(transaction.getInputs().contains(input1));
        assertTrue(transaction.getInputs().contains(input2));
        assertTrue(transaction.getOutputs().contains(output1));
        assertTrue(transaction.getOutputs().contains(output2));
    }

    @Test
    void createTransaction_WithNullInputs_ShouldFail() {
        Xput output = new Xput(testAddress1, 50L * Constants.BITCOIN);

        assertThrows(RuntimeException.class, () -> 
            new Transaction(null, List.of(output), "invalid_tx"));
    }

    @Test
    void createTransaction_WithEmptyInputs_ShouldFail() {
        Xput output = new Xput(testAddress1, 50L * Constants.BITCOIN);

        assertThrows(RuntimeException.class, () -> 
            new Transaction(List.of(), List.of(output), "invalid_tx"));
    }

    @Test
    void createTransaction_WithNullOutputs_ShouldFail() {
        Xput input = new Xput(testAddress1, 100L * Constants.BITCOIN);

        assertThrows(RuntimeException.class, () -> 
            new Transaction(List.of(input), null, "invalid_tx"));
    }

    @Test
    void createTransaction_WithEmptyOutputs_ShouldFail() {
        Xput input = new Xput(testAddress1, 100L * Constants.BITCOIN);

        assertThrows(RuntimeException.class, () -> 
            new Transaction(List.of(input), List.of(), "invalid_tx"));
    }

    @Test
    void createTransaction_WithNullHash_ShouldSucceed() {
        Xput input = new Xput(testAddress1, 100L * Constants.BITCOIN);
        Xput output = new Xput(testAddress2, 90L * Constants.BITCOIN);

        Transaction transaction = new Transaction(List.of(input), List.of(output), null);

        assertNull(transaction.getHash());
    }

    @Test
    void createTransaction_WithEmptyHash_ShouldSucceed() {
        Xput input = new Xput(testAddress1, 100L * Constants.BITCOIN);
        Xput output = new Xput(testAddress2, 90L * Constants.BITCOIN);

        Transaction transaction = new Transaction(List.of(input), List.of(output), "");

        assertEquals("", transaction.getHash());
    }

    @Test
    void validate_WithValidTransaction_ShouldNotThrow() {
        Xput input = new Xput(testAddress1, 100L * Constants.BITCOIN);
        Xput output = new Xput(testAddress2, 90L * Constants.BITCOIN);

        // Should not throw an exception
        assertDoesNotThrow(() -> 
            new Transaction(List.of(input), List.of(output), "valid_tx"));
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        Xput input = new Xput(testAddress1, 100L * Constants.BITCOIN);
        Xput output = new Xput(testAddress2, 90L * Constants.BITCOIN);
        String hash = "test_hash_123";
        List<Xput> inputs = List.of(input);
        List<Xput> outputs = List.of(output);

        Transaction transaction = new Transaction(inputs, outputs, hash);

        assertEquals(inputs, transaction.getInputs());
        assertEquals(outputs, transaction.getOutputs());
        assertEquals(hash, transaction.getHash());
    }
}