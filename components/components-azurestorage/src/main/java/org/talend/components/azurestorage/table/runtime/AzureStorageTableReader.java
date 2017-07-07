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
import org.talend.components.azurestorage.table.AzureStorageTableService;
import org.talend.components.azurestorage.table.avro.AzureStorageAvroRegistry;
import org.talend.components.azurestorage.table.avro.AzureStorageTableAdaptorFactory;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.TableQuery;

public class AzureStorageTableReader extends AbstractBoundedReader<IndexedRecord> {

    private TAzureStorageInputTableProperties properties;

    private transient DynamicTableEntity current;

    private transient Iterator<DynamicTableEntity> recordsIterator;

    private transient Schema querySchema;

    private transient AzureStorageTableAdaptorFactory factory;

    private transient Map<String, String> nameMappings;

    private boolean started;

    private Boolean advanceable;

    private Result result;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageTableReader.class);

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageTableReader.class);

    public AzureStorageTableService tableService;

    public AzureStorageTableReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageInputTableProperties properties) {
        super(source);
        this.properties = properties;
        this.nameMappings = properties.nameMapping.getNameMappings();
        this.tableService = new AzureStorageTableService(((AzureStorageTableSource) source).getAzureConnection(container));
        this.result = new Result();
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

        String tableName = properties.tableName.getValue();
        String filter = "";
        if (properties.useFilterExpression.getValue()) {
            filter = properties.filterExpression.generateCombinedFilterConditions();
            LOGGER.debug(i18nMessages.getMessage("debug.FilterApplied", filter));
        }
        try {
            TableQuery<DynamicTableEntity> partitionQuery;
            if (filter.isEmpty()) {
                partitionQuery = TableQuery.from(DynamicTableEntity.class);
            } else {
                partitionQuery = TableQuery.from(DynamicTableEntity.class).where(filter);
            }
            // Using execute will automatically and lazily follow the continuation tokens from page to page of results.
            // So, we bypass the 1000 entities limit.
            Iterable<DynamicTableEntity> entities = tableService.executeQuery(tableName, partitionQuery);
            recordsIterator = entities.iterator();
            if (recordsIterator.hasNext()) {
                started = true;
                result.totalCount++;
                current = recordsIterator.next();
            }
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue()) {
                throw new ComponentException(e);
            }
        }

        return started;
    }

    @Override
    public boolean advance() throws IOException {
        if (!started) {
            advanceable = false;
            return false;
        }

        advanceable = recordsIterator.hasNext();
        if (advanceable) {
            result.totalCount++;
            current = recordsIterator.next();
        }

        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (!started || (advanceable != null && !advanceable)) {
            throw new NoSuchElementException();
        }

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
        return result.toMap();
    }
}
