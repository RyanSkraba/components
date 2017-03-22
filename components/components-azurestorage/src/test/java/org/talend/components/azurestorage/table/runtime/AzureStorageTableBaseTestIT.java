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

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageBaseTestIT;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.microsoft.azure.storage.table.CloudTableClient;

public abstract class AzureStorageTableBaseTestIT extends AzureStorageBaseTestIT {

    public String pk_test1 = "partitionKey1";

    public String pk_test2 = "partitionKey2";

    public String pk_test3 = "partitionKey3";

    public String rk_test1 = "rowKey1";

    public String rk_test2 = "rowKey2";

    public String rk_test3 = "rowKey3";

    public String rk_test4 = "rowKey4";

    public String rk_test5 = "rowKey5";

    public String tbl_test = "testTable";

    public String[] partitions = { pk_test1, pk_test2, pk_test3 };

    public String[] rows = { rk_test1, rk_test2, rk_test3 };

    public String[] rows_all = { rk_test1, rk_test2, rk_test3, rk_test4, rk_test5 };

    List<String> schemaMappings = new ArrayList<>();

    List<String> propertyMappings = new ArrayList<>();

    protected AzureStorageTableSink sink;

    protected CloudTableClient tableClient;

    public AzureStorageTableBaseTestIT(String testName) {
        super(testName);

        TAzureStorageOutputTableProperties properties = new TAzureStorageOutputTableProperties("tests");
        properties = (TAzureStorageOutputTableProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.setupProperties();
        properties.tableName.setValue(tbl_test);
        properties.actionOnTable.setValue(ActionOnTable.Create_table_if_does_not_exist);

        try {
            sink = new AzureStorageTableSink();
            sink.initialize(null, properties);
            sink.validate(null);
            tableClient = sink.getStorageTableClient(null);
        } catch (InvalidKeyException | URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public Writer<?> createWriter(ComponentProperties properties) {
        sink.initialize(null, properties);
        sink.validate(null);
        return sink.createWriteOperation().createWriter(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BoundedReader<T> createBoundedReader(ComponentProperties props) {
        AzureStorageTableSource source = new AzureStorageTableSource();
        source.initialize(null, props);
        source.validate(null);
        return source.createReader(null);
    }

    public Schema getDynamicSchema() {
        Schema emptySchema = Schema.createRecord("dynamic", null, null, false);
        emptySchema.setFields(new ArrayList<Schema.Field>());
        emptySchema = AvroUtils.setIncludeAllFields(emptySchema, true);
        return emptySchema;
    }

    public Schema getSystemSchema() {
        return SchemaBuilder.record("Main").fields()
                //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("Timestamp").prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "20")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._date()).noDefault()
                //
                .endRecord();
    }

}
