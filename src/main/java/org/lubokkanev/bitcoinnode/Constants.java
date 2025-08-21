package org.lubokkanev.bitcoinnode;

public class Constants {
    public static final long SATOSHI = 1L;
    public static final long BIT = 100 * SATOSHI;
    public static final long M_BIT = 1000 * SATOSHI;
    public static final long BITCOIN = 100000000 * SATOSHI;

    public static final String COINBASE_ADDRESS = "coinbase";

    // Difficulty Adjustment (simplified for this project)
    // Base difficulty in leading zero bits and interval in blocks
    public static final long BASE_DIFFICULTY = 3L;
    public static final long DIFFICULTY_ADJUSTMENT_INTERVAL = 10L;

    // Discrete retarget parameters (Bitcoin-like, but simplified)
    public static final long TARGET_BLOCK_TIME_SEC = 10L; // target time per block
    public static final double DAA_MIN_ADJUSTMENT = 0.25; // clamp lower bound
    public static final double DAA_MAX_ADJUSTMENT = 4.0;  // clamp upper bound
    public static final long MIN_DIFFICULTY_BITS = 1L;
    public static final long MAX_DIFFICULTY_BITS = 255L;
}
