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

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.Collections;
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
 * <li>the simplified logic for reading is found in the {@link DataSetReader}, and</li>
 * </ul>
 */
public class DataSetSource implements BoundedSource {

    /** Default serial version UID. */
    private static final long serialVersionUID = -3740291007255450917L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetSource.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(DataSetSource.class);

    /** Configuration extracted from the input properties. */
    private RuntimeProperties runtimeProperties;

    private transient Schema schema;

    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        this.runtimeProperties = ((TDataSetInputProperties) properties).getRuntimeProperties();
        schema = new Schema.Parser().parse(runtimeProperties.getSchema());
    }

    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        return new DataSetReader(container, this, getConnectionHandler(), this.schema);
    }

    private DataPrepConnectionHandler getConnectionHandler() {
        return new DataPrepConnectionHandler(runtimeProperties.getUrl(), //
                runtimeProperties.getLogin(), //
                runtimeProperties.getPass(), //
                runtimeProperties.getDataSetName());
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            getConnectionHandler().validate();
        } catch (IOException e) {
            LOGGER.debug(messages.getMessage("error.validationFailed", e));
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(messages.getMessage("error.validationFailed", e));
        }
        return ValidationResult.OK;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return Collections.emptyList();
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
