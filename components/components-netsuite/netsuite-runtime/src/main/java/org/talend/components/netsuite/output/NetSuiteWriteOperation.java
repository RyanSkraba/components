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

package org.talend.components.netsuite.output;

import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.NetSuiteSink;
import org.talend.components.netsuite.SchemaCustomMetaDataSource;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;

/**
 *
 */
public class NetSuiteWriteOperation implements WriteOperation<Result> {

    private final NetSuiteSink sink;
    private NetSuiteOutputProperties properties;

    public NetSuiteWriteOperation(NetSuiteSink sink, NetSuiteOutputProperties properties) {
        this.sink = sink;
        this.properties = properties;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        // do nothing
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        NetSuiteClientService clientService = sink.getClientService();

        OutputAction action = properties.module.action.getValue();

        Schema schema = properties.module.main.schema.getValue();

        MetaDataSource originalMetaDataSource = clientService.getMetaDataSource();
        MetaDataSource metaDataSource = clientService.createDefaultMetaDataSource();
        metaDataSource.setCustomizationEnabled(originalMetaDataSource.isCustomizationEnabled());
        SchemaCustomMetaDataSource schemaCustomMetaDataSource = new SchemaCustomMetaDataSource(
                clientService.getBasicMetaData(), originalMetaDataSource.getCustomMetaDataSource(), schema);
        metaDataSource.setCustomMetaDataSource(schemaCustomMetaDataSource);

        NetSuiteOutputWriter<?, ?> writer;
        switch (action) {
        case ADD:
            writer = new NetSuiteAddWriter<>(this, metaDataSource);
            break;
        case UPDATE:
            writer = new NetSuiteUpsertWriter<>(this, metaDataSource);
            break;
        case UPSERT:
            writer = new NetSuiteUpsertWriter<>(this, metaDataSource);
            Boolean useNativeUpsert = properties.module.useNativeUpsert.getValue();
            if (useNativeUpsert != null) {
                ((NetSuiteUpsertWriter) writer).setUseNativeUpsert(useNativeUpsert);
            }
            break;
        case DELETE:
            writer = new NetSuiteDeleteWriter<>(this, metaDataSource);
            break;
        default:
            throw new NetSuiteException("Output operation not implemented: " + action);
        }

        Integer batchSize = properties.batchSize.getValue();
        if (batchSize != null) {
            writer.setBatchSize(batchSize);
        }

        return writer;
    }

    @Override
    public NetSuiteSink getSink() {
        return sink;
    }

    public NetSuiteOutputProperties getProperties() {
        return properties;
    }
}
