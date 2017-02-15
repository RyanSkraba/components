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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;

public class AzureStorageTableWriteOperationTest {

    private AzureStorageTableWriteOperation azureStorageTableWriteOperation;

    @Test
    public void testASTWO() throws Exception {
        azureStorageTableWriteOperation = new AzureStorageTableWriteOperation(new AzureStorageTableSink());
        azureStorageTableWriteOperation.initialize(null);
        TAzureStorageOutputTableProperties p = new TAzureStorageOutputTableProperties("test");
        p.setupProperties();
        p.tableName.setValue("test");
        azureStorageTableWriteOperation.getSink().properties = p;
        assertNotNull(azureStorageTableWriteOperation.getSink());
        assertNotNull(azureStorageTableWriteOperation.createWriter(null));
        List<Result> writerResults = new ArrayList<Result>();
        Result r = new Result("test");
        writerResults.add(r);
        assertNotNull(azureStorageTableWriteOperation.finalize(writerResults, null));
    }

}
