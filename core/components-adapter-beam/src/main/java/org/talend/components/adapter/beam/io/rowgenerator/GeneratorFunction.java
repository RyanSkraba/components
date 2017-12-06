// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.adapter.beam.io.rowgenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.beam.sdk.transforms.SerializableFunction;

/**
 * The generator function is Serializable, and turns the current state of the GeneratorContext into an Avro datum.
 *
 * @param <T> The expected type of the datum.
 */
public abstract class GeneratorFunction<T> implements SerializableFunction<GeneratorFunction.GeneratorContext, T> {

    /**
     * A mutable, reusable context for row generation. The internal state of the pseudorandom generator is expected to
     * change, and the row ID to be updated can be modified.
     */
    public static class GeneratorContext {

        private final int partitionId;

        /** The seed to use in generating pseudo-random numbers. */
        private Long seed;

        private long rowId;

        private Random random;

        private GeneratorContext(int partitionId, Long seed) {
            this.partitionId = partitionId;
            this.seed = seed;
        }

        /**
         * Factory method to create a {@link GeneratorContext}. The rowId and random properties *must* be set before
         * use.
         *
         * @param partitionId The partitionId to set for this context.
         * @param seed If non-null, a seed to use for the random number generator.
         * @return A reusable instance to use in creating rows.
         */
        public static GeneratorContext of(int partitionId, Long seed) {
            return new GeneratorContext(partitionId, seed);
        }

        public Random getRandom() {
            if (random == null) {
                if (seed == null) {
                    random = new Random(System.currentTimeMillis() + partitionId * 24 * 60 * 60 * 1000L);
                } else {
                    random = new Random(scramble(seed + rowId));
                }
            }
            return random;
        }

        public int getPartitionId() {
            return partitionId;
        }

        public long getRowId() {
            return rowId;
        }

        public void setRowId(long rowId) {
            this.rowId = rowId;
            // Regenerate the random number generator the next time it is used.
            if (seed != null) {
                random = null;
            }
        }

        /**
         * Gets an unpredictable but deterministic long from the input seed.
         * <p>
         * Two input seeds with a small bit difference should have much different outputs (a.k.a. the avalanche effect
         * in message digests). This can be useful for generating many different PRNG from a sequence of IDs such that
         * each generates pseudorandom numbers that look independent of the others. Using the sequence as seeds
         * directly would otherwise cause visible patterns in the output.
         *
         * @param inSeed the input seed
         * @return an unpredictable but deterministic long such that a minor bit change in the input causes a much different
         * output in the long.
         */
        private static long scramble(long inSeed) {
            try {
                long out = inSeed;
                // Turn the seed into a byte array to pass through MD5.
                MessageDigest d = MessageDigest.getInstance("MD5");
                byte[] bytes = new byte[Long.SIZE / Byte.SIZE];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) (out & 0xFF);
                    out >>= Byte.SIZE;
                }
                // Digest the bytes.
                bytes = d.digest(bytes);
                // Turn the byte array back into a seed.
                for (int i = 0; i < Math.min(bytes.length, Long.SIZE); i++) {
                    out <<= 8;
                    out |= (bytes[i] & 0xFF);
                }
                return out;
            } catch (NoSuchAlgorithmException e) {
                // The digest must exist according to the Java spec, but if it doesn't, then just use the
                // newSeed without hashing.
                return inSeed;
            }
        }
    }

}
