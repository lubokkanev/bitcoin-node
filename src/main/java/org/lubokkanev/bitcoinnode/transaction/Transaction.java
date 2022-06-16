package org.lubokkanev.bitcoinnode.transaction;

import java.util.List;

public class Transaction {
    private List<Xput> outputs;
    private List<Xput> inputs;

    private String hash;

    public Transaction(List<Xput> inputs, List<Xput> outputs, String hash) {
        this.outputs = outputs;
        this.inputs = inputs;
        this.hash = hash;

        validate();
    }

    public List<Xput> getOutputs() {
        return outputs;
    }

    public List<Xput> getInputs() {
        return inputs;
    }

    public String getHash() {
        return hash;
    }

    public void validate() {
        if (inputs == null || inputs.size() == 0 || outputs == null || outputs.size() == 0) {
            throw new RuntimeException("Invalid transaction.");
        }
    }
}
