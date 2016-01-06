// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.facet;

import java.io.IOException;
import java.util.List;

import org.talend.components.api.runtime.ReturnObject;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.coders.EntityCoder;
import com.google.cloud.dataflow.sdk.io.BoundedSource;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.common.collect.ImmutableList;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class SimpleInputFacet extends BoundedSource<Entity> implements ComponentFacet {

    /**
     * Connect to an external database
     */
    public abstract void connection();

    /**
     * Retrieve information from the database and put them into the return object
     *
     * @param returnObject Object that know how to correctly return the current object for any runtime
     * @throws Exception
     */
    public abstract void execute(ReturnObject returnObject) throws Exception;

    /**
     * Close the connection to the database
     */
    public abstract void tearDown();

    @Override
    public com.google.cloud.dataflow.sdk.io.BoundedSource.BoundedReader<Entity> createReader(PipelineOptions arg0)
            throws IOException {
        // TODO Fix that.
        return new AbstractReader(this) {

            @Override
            public boolean start() throws IOException {
                return false;
            }

            @Override
            public Entity getCurrent() {
                return null;
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public boolean advance() throws IOException {
                return false;
            }
        };
    }

    // TODO set as abstract an be overrided by the component implementation
    @Override
    public long getEstimatedSizeBytes(PipelineOptions arg0) throws Exception {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(PipelineOptions arg0) throws Exception {
        return false;
    }

    // TODO set as abstract an be overrided by the component implementation
    @Override
    public List<? extends BoundedSource<Entity>> splitIntoBundles(long arg0, PipelineOptions arg1) throws Exception {
        return ImmutableList.of(this);
    }

    @Override
    public Coder<Entity> getDefaultOutputCoder() {
        return EntityCoder.of();
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull("ObjectToCheck", "ErrorMessage");
    }

    // TODO maybe we can get ride of AbsbtractReader an direclty implement the BoundedReader in the component when they
    // require one.
    public abstract static class AbstractReader extends BoundedSource.BoundedReader<Entity> {

        private final SimpleInputFacet source;

        /**
         * Returns a DatastoreReader with Source and Datastore object set.
         *
         * @param datastore a datastore connection to use.
         */
        public AbstractReader(SimpleInputFacet source) {
            this.source = source;
        }

        @Override
        public abstract Entity getCurrent();

        @Override
        public abstract boolean start() throws IOException;

        @Override
        public abstract boolean advance() throws IOException;

        @Override
        public abstract void close() throws IOException;

        @Override
        public SimpleInputFacet getCurrentSource() {
            return source;
        }

        @Override
        public SimpleInputFacet splitAtFraction(double fraction) {
            // Not supported.
            return null;
        }

        @Override
        public Double getFractionConsumed() {
            // Not supported.
            return null;
        }
    }
}
