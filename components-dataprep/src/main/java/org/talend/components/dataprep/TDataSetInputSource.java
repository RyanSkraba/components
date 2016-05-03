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
package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
 * <li>the simplified logic for reading is found in the {@link TDataSetInputReader}, and</li>
 * </ul>
 */
public class TDataSetInputSource implements BoundedSource {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Configuration extracted from the input properties. */
    private TDataSetInputProperties properties;

    private transient Schema schema;

    private DataPrepConnectionHandler connectionHandler;

    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (TDataSetInputProperties) properties;
        schema = new Schema.Parser().parse(this.properties.schema.schema.getStringValue());
    }

    public BoundedReader createReader(RuntimeContainer container) {
        return new TDataSetInputReader(container, this, getConnectionHandler(), this.schema);
    }

    private DataPrepConnectionHandler getConnectionHandler() {
        if (connectionHandler == null) {
            connectionHandler = new DataPrepConnectionHandler(properties.url.getStringValue(), properties.login.getStringValue(),
                    properties.pass.getStringValue(), "read", properties.dataSetName.getStringValue());
            return connectionHandler;
        } else
            return this.connectionHandler;
    }

    public ValidationResult validate(RuntimeContainer container) {
        boolean validate;
        try {
            validate = getConnectionHandler().validate();
        } catch (IOException e) {
            validate = false;
        }
        if (validate)
            return ValidationResult.OK;
        else
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR);
    }

    public Schema getSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        // There can be only one.
        return Arrays.asList(this);
    }

    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        // This will be ignored since the source will never be split.
        return 0;
    }

    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

}
