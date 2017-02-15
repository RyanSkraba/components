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

import java.io.Serializable;
import java.util.Random;

import com.google.common.base.Function;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;

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

        private long rowId;

        private Random random;

        private GeneratorContext(int partitionId) {
            this.partitionId = partitionId;
        }

        /**
         * Factory method to create a {@link GeneratorContext}. The rowId and random properties *must* be set before
         * use.
         * 
         * @param partitionId The partitionId to set for this context.
         * @return A reusable instance to use in creating rows.
         */
        public static GeneratorContext of(int partitionId) {
            return new GeneratorContext(partitionId);
        }

        public Random getRandom() {
            return random;
        }

        public void setRandom(Random random) {
            this.random = random;
        }

        public int getPartitionId() {
            return partitionId;
        }

        public long getRowId() {
            return rowId;
        }

        public void setRowId(long rowId) {
            this.rowId = rowId;
        }
    }

}
