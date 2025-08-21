package org.lubokkanev.bitcoinnode.block;

import org.junit.jupiter.api.Test;

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
}


