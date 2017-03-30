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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.runtime.AzureStorageSourceOrSink;
import org.talend.components.azurestorage.table.AzureStorageTableProperties;
import org.talend.components.azurestorage.table.avro.AzureStorageAvroRegistry;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.TableQuery;

public class AzureStorageTableSourceOrSink extends AzureStorageSourceOrSink implements SourceOrSink {

    public static final String PARTITION_KEY = "PartitionKey";

    public static final String ROW_KEY = "RowKey";

    public static final String TIMESTAMP = "Timestamp";

    private static final long serialVersionUID = 5588144102302302129L;

    private final Pattern tableCheckNamePattern = Pattern.compile("^[A-Za-z][A-Za-z0-9]{2,62}$");

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageTableSourceOrSink.class);

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageTableSourceOrSink.class);

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult vr = super.validate(container);
        if (vr.getStatus() == ValidationResult.Result.ERROR) {
            return vr;
        }

        AzureStorageTableProperties p = (AzureStorageTableProperties) properties;
        String tn = p.tableName.getValue();
        if (tn.isEmpty()) {
            vr = new ValidationResult();
            vr.setStatus(ValidationResult.Result.ERROR);
            vr.setMessage(i18nMessages.getMessage("message.VacantName"));
            return vr;
        }
        if (!tableCheckNamePattern.matcher(tn).matches()) {
            vr = new ValidationResult();
            vr.setStatus(ValidationResult.Result.ERROR);
            vr.setMessage(i18nMessages.getMessage("message.IncorrectName"));
            return vr;
        }

        return ValidationResult.OK;
    }

    public static List<NamedThing> getSchemaNames(RuntimeContainer container, TAzureStorageConnectionProperties properties)
            throws IOException {
        AzureStorageTableSourceOrSink sos = new AzureStorageTableSourceOrSink();
        sos.initialize(container, properties);
        return sos.getSchemaNames(container);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> result = new ArrayList<>();
        try {
            CloudTableClient client = getStorageTableClient(container);
            for (String t : client.listTables()) {
                result.add(new SimpleNamedThing(t, t));
            }
        } catch (InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new ComponentException(e);
        }
        return result;
    }

    public static Schema getSchema(RuntimeContainer container, TAzureStorageConnectionProperties properties, String schemaName)
            throws IOException {
        AzureStorageTableSourceOrSink sos = new AzureStorageTableSourceOrSink();
        sos.initialize(container, properties);
        return sos.getEndpointSchema(container, schemaName);
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        CloudTable table;
        try {
            table = getStorageTableReference(container, schemaName);
            TableQuery<DynamicTableEntity> partitionQuery;
            partitionQuery = TableQuery.from(DynamicTableEntity.class).take(1);
            Iterable<DynamicTableEntity> entities = table.execute(partitionQuery);
            DynamicTableEntity result = entities.iterator().next();
            return AzureStorageAvroRegistry.get().inferSchema(result);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new ComponentException(e);
        }
    }

    public CloudTableClient getStorageTableClient(RuntimeContainer runtime) throws InvalidKeyException, URISyntaxException {
        return getStorageAccount(runtime).createCloudTableClient();
    }

    public CloudTable getStorageTableReference(RuntimeContainer runtime, String tableName)
            throws InvalidKeyException, URISyntaxException, StorageException {
        return getStorageTableClient(runtime).getTableReference(tableName);
    }
}
