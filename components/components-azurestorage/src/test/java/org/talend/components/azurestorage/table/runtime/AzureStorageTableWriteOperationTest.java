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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.table.TableHelper;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnData;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageTableWriteOperationTest {

    private AzureStorageTableWriteOperation azureStorageTableWriteOperation;

    private TAzureStorageOutputTableProperties properties;

    public static final String PROP_ = "PROP_";

    private RuntimeContainer container;

    private AzureStorageTableSink sink;

    @Before
    public void setUp() throws Exception {

        container = new RuntimeContainerMock();
        sink = new AzureStorageTableSink();

        properties = new TAzureStorageOutputTableProperties(PROP_ + "InputTable");
        properties.setupProperties();
        // valid fake connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        properties.dieOnError.setValue(false);
        properties.tableName.setValue("testtable");
        properties.actionOnTable.setValue(ActionOnTable.Create_table_if_does_not_exist);
        properties.actionOnData.setValue(ActionOnData.Insert);

        properties.schema.schema.setValue(TableHelper.getWriteSchema());
        properties.schemaReject.schema.setValue(TableHelper.getRejectSchema());
        properties.partitionKey.setStoredValue("PartitionKey");
        properties.rowKey.setStoredValue("RowKey");

    }

    @Test
    public void testASTWO() throws Exception {
        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());

        azureStorageTableWriteOperation = (AzureStorageTableWriteOperation) sink.createWriteOperation();

        assertNotNull(azureStorageTableWriteOperation.getSink());

        List<Result> writerResults = new ArrayList<Result>();
        Result r = new Result("test");
        writerResults.add(r);
        assertNotNull(azureStorageTableWriteOperation.finalize(writerResults, container));
    }

}
