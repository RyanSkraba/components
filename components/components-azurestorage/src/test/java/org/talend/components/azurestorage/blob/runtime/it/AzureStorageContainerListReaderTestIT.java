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
package org.talend.components.azurestorage.blob.runtime.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

@Ignore
public class AzureStorageContainerListReaderTestIT extends AzureStorageBaseBlobTestIT {

    Schema s = SchemaBuilder.record("Main").fields().name("ContainerName").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "50")// $NON-NLS-3$
            .type(AvroUtils._string()).noDefault().endRecord();

    public AzureStorageContainerListReaderTestIT() {
        super("container-list-" + getRandomTestUID());
    }

    @Before
    public void createTestContainers() throws Exception {
        for (String c : TEST_CONTAINERS) {
            doContainerCreate(getNamedThingForTest(c), TAzureStorageContainerCreateProperties.AccessControl.Private);
        }
    }

    @After
    public void deleteTestContainers() throws Exception {
        for (String c : TEST_CONTAINERS) {
            doContainerDelete(getNamedThingForTest(c));
        }
    }

    /**
     * isTestContainerInContainerList.
     *
     * @param containers containers in storage
     * @return {@link Boolean} boolean
     */
    public Boolean isTestContainerInContainerList(List<String> containers) {
        Boolean result = true;
        for (String c : TEST_CONTAINERS)
            for (String lc : containers)
                if (lc == getNamedThingForTest(c))
                    result = result && true;

        return result;
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testContainerList() throws Exception {
        BoundedReader reader = createContainerListReader();
        List<String> containers = new ArrayList<>();
        Boolean rows = reader.start();
        Object row;
        assertTrue(rows);
        //
        while (rows) {
            row = ((IndexedRecord) reader.getCurrent()).get(0);
            assertNotNull(row);
            assertTrue(row instanceof String);
            containers.add(row.toString());
            rows = reader.advance();
        }
        reader.close();
        assertTrue(isTestContainerInContainerList(containers));
    }
}
