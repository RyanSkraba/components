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
package org.talend.components.azurestorage.table.runtime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.table.avro.AzureStorageAvroRegistry;
import org.talend.components.azurestorage.table.avro.AzureStorageTableAdaptorFactory;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.daikon.avro.AvroUtils;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.TableQuery;

public class AzureStorageTableReader extends AbstractBoundedReader<IndexedRecord> {

    private TAzureStorageInputTableProperties properties;

    private RuntimeContainer runtime;

    private int dataCount;

    private transient DynamicTableEntity current;

    private transient Iterator<DynamicTableEntity> results;

    private transient Schema querySchema;

    private transient AzureStorageTableAdaptorFactory factory;

    private transient Map<String, String> nameMappings;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageTableReader.class);

    public AzureStorageTableReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageInputTableProperties properties) {
        super(source);
        this.runtime = container;
        this.properties = properties;
        this.nameMappings = properties.nameMapping.getNameMappings();
    }

    private Schema getSchema() throws IOException {
        Schema designSchema = properties.schema.schema.getValue();
        if (designSchema != null) {
            if (AvroUtils.isIncludeAllFields(designSchema)) {
                querySchema = AzureStorageAvroRegistry.get().inferSchema(current);
            } else {
                querySchema = designSchema;
            }
        } else {
            querySchema = AzureStorageAvroRegistry.get().inferSchema(current);
        }
        return querySchema;
    }

    private AzureStorageTableAdaptorFactory getFactory() throws IOException {
        if (null == factory) {
            factory = new AzureStorageTableAdaptorFactory(nameMappings);
            factory.setSchema(getSchema());
        }
        return factory;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable = Boolean.FALSE;
        String tableName = properties.tableName.getValue();
        String filter = "";
        if (properties.useFilterExpression.getValue()) {
            filter = properties.filterExpression.getCombinedFilterConditions();
            LOGGER.debug("Filter applied : {}.", filter);
        }
        try {
            CloudTable table = ((AzureStorageTableSource) getCurrentSource()).getStorageTableReference(runtime, tableName);
            TableQuery<DynamicTableEntity> partitionQuery;
            if (filter.isEmpty()) {
                partitionQuery = TableQuery.from(DynamicTableEntity.class);
            } else {
                partitionQuery = TableQuery.from(DynamicTableEntity.class).where(filter);
            }
            // Using execute will automatically and lazily follow the continuation tokens from page to page of results.
            // So, we bypass the 1000 entities limit.
            Iterable<DynamicTableEntity> entities = table.execute(partitionQuery);
            results = entities.iterator();
            startable = results.hasNext();
            if (startable) {
                dataCount++;
                current = results.next();
            }
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue()) {
                throw new ComponentException(e);
            }
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        Boolean advanceable = results.hasNext();
        if (advanceable) {
            dataCount++;
            current = results.next();
        }
        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        try {
            return getFactory().convertToAvro(current);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue()) {
                throw new ComponentException(e);
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result res = new Result();
        res.totalCount = dataCount;

        return res.toMap();
    }
}
