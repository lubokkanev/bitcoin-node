package org.lubokkanev.bitcoinnode.transaction;

import static org.lubokkanev.bitcoinnode.Constants.COINBASE_ADDRESS;

public class Xput {
    private Address address;
    private long amountSats;

    public Xput(Address address, long amountSats) {
        this.address = address;
        this.amountSats = amountSats;
    }

    public boolean isCoinbase() {
        return COINBASE_ADDRESS.equals(address.getCashAddress()) || COINBASE_ADDRESS.equals(address.getPubKeyHash());
    }
}

// from address A (output1(5sats) output2(6sats)) send 7 sats to address B