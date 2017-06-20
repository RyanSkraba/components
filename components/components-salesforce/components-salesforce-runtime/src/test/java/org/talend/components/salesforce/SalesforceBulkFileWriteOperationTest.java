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

package org.talend.components.salesforce;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.common.BulkFileProperties;
import org.talend.components.common.runtime.BulkFileSink;
import org.talend.components.salesforce.runtime.SalesforceBulkFileWriteOperation;

/**
 *
 */
public class SalesforceBulkFileWriteOperationTest {

    private BulkFileProperties properties;

    private BulkFileSink sink;

    private SalesforceBulkFileWriteOperation writeOperation;

    @Before
    public void setUp() {
        properties = new BulkFileProperties("root");

        sink = new BulkFileSink();

        writeOperation = new SalesforceBulkFileWriteOperation(sink);
    }

    @Test
    public void testCreateWriter() throws IOException {
        properties.init();

        sink.initialize(null, properties);

        Writer<Result> writer = writeOperation.createWriter(null);
        assertEquals(writeOperation, writer.getWriteOperation());
    }
}
