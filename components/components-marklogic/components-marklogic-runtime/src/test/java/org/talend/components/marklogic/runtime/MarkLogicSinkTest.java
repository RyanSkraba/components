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
