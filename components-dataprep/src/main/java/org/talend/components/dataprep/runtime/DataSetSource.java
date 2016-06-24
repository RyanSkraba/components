// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep.runtime;

import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputProperties;

/**
 * The TDataSetInputSource provides the mechanism to supply data to other components at run-time.
 *
 * Based on the Apache Beam project, the Source mechanism is appropriate to describe distributed and non-distributed
 * data sources and can be adapted to scalable big data execution engines on a cluster, or run locally.
 *
 * This example component describes an input source that is guaranteed to be run in a single JVM (whether on a cluster
 * or locally), so:
 *
 * <ul>
 * <li>the simplified logic for reading is found in the {@link DataSetReader}, and</li>
 * </ul>
 */
public class DataSetSource extends DataSetSourceOrSink implements BoundedSource {

    /** Default serial version UID. */
    private static final long serialVersionUID = -3740291007255450917L;

    private transient Schema schema;

    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        runtimeProperties = ((TDataSetInputProperties) properties).getRuntimeProperties();
        schema = new Schema.Parser().parse(runtimeProperties.getSchema());
    }

    public Schema getSchema() {
        return schema;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        return new DataSetReader(this);
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        // There can be only one.
        return Collections.singletonList(this);
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        // This will be ignored since the source will never be split.
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

}
