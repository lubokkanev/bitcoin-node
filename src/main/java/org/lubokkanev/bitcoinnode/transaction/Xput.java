package org.lubokkanev.bitcoinnode.transaction;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Xput xput = (Xput) o;
        return amountSats == xput.amountSats && Objects.equals(address, xput.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, amountSats);
    }
}

// from address A (output1(5sats) output2(6sats)) send 7 sats to address B