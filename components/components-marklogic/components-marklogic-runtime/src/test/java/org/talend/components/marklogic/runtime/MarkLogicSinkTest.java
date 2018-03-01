// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.marklogic.runtime;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputProperties;

import static org.junit.Assert.assertEquals;

public class MarkLogicSinkTest {

    private MarkLogicSink sink;

    @Before
    public void setUp() {
        sink = new MarkLogicSink();
    }

    @Test
    public void testCreateWriteOperation() {
        MarkLogicOutputProperties outputProperties = new MarkLogicOutputProperties("outProps");
        sink.initialize(null, outputProperties);

        MarkLogicWriteOperation writeOperation = sink.createWriteOperation();

        assertEquals(sink, writeOperation.getSink());
    }

    @Test(expected = MarkLogicException.class)
    public void testCreateWriteOperationWithWrongProperties() {
        MarkLogicInputProperties fakeOutputProperties = new MarkLogicInputProperties("fakeOutProps");
        sink.initialize(null, fakeOutputProperties);

        sink.createWriteOperation();
    }

}
