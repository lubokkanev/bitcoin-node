package org.lubokkanev.bitcoinnode.block;

import org.junit.jupiter.api.Test;
import static org.lubokkanev.bitcoinnode.Constants.DIFFICULTY_ADJUSTMENT_INTERVAL;
import static org.lubokkanev.bitcoinnode.Constants.TARGET_BLOCK_TIME_SEC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockDifficultyTest {
    private static long countLeadingZeroBits(byte[] bytes) {
        long count = 0;
        for (byte b : bytes) {
            int unsigned = b & 0xFF;
            if (unsigned == 0) {
                count += 8;
            } else {
                count += Integer.numberOfLeadingZeros(unsigned) - 24;
                break;
            }
        }
        return count;
    }

    @Test
    public void minedHashMeetsDifficulty() throws Exception {
        Block genesis = new Block();
        Block block = new Block(genesis);

        block.findNonce();

        long leadingZeros = countLeadingZeroBits(block.getHash());
        assertTrue(leadingZeros >= block.getDifficulty(),
                "Expected leading zero bits >= difficulty, got " + leadingZeros + " vs " + block.getDifficulty());
    }

    @Test
    public void difficultyMonotonicIncreaseByHeight() {
        Block b1 = new Block();
        Block b2 = new Block(b1);
        Block b3 = new Block(b2);

        long d1 = b1.getDifficulty();
        long d2 = b2.getDifficulty();
        long d3 = b3.getDifficulty();

        assertTrue(d2 >= d1);
        assertTrue(d3 >= d2);
    }

    

    @Test
    public void retargetsAtIntervalUsingSolveTimes() {
        // Build a chain of interval blocks with slow solve times (worse than target)
        Block prev = new Block();
        long interval = DIFFICULTY_ADJUSTMENT_INTERVAL;

        // Seed timestamps for deterministic tests
        long t = 1_000_000L;
        prev.setTimestampSec(t);

        for (int i = 1; i < interval; i++) {
            Block next = new Block(prev);
            t += TARGET_BLOCK_TIME_SEC * 2; // twice as slow as target
            next.setTimestampSec(t);
            prev = next;
        }

        long before = prev.getDifficulty();

        // Next block is a retarget point
        Block retarget = new Block(prev);
        t += TARGET_BLOCK_TIME_SEC * 2;
        retarget.setTimestampSec(t);

        long after = retarget.getDifficulty();

        // Since blocks were slower, difficulty in leading-zero bits should decrease or stay same
        // (easier target -> fewer required leading zeros)
        assertTrue(after <= before);
    }

    @Test
    public void noRetargetWithinIntervalUsesPreviousDifficulty() {
        Block b1 = new Block();
        b1.setTimestampSec(1000);
        Block b2 = new Block(b1);
        b2.setTimestampSec(1005);

        // Not a retarget boundary yet, difficulty should equal previous
        assertEquals(b1.getDifficulty(), b2.getDifficulty());
    }
}


