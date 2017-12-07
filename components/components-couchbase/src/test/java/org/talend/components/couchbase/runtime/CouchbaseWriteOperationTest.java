package org.talend.components.couchbase.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;

public class CouchbaseWriteOperationTest {

    private CouchbaseWriteOperation writeOperation;
    private CouchbaseSink sink;

    @Before
    public void setup() {
        sink = new CouchbaseSink();
        writeOperation = new CouchbaseWriteOperation(sink);
    }

    @Test
    public void testFinalize() {
        Set<Result> resultList = new HashSet<>(2);
        Result result1 = new Result("result1", 2, 1, 1);
        resultList.add(result1);
        Result result2 = new Result("result2", 5, 3, 2);
        resultList.add(result2);

        Map<String, Object> resultMap = writeOperation.finalize(resultList, null);

        Assert.assertEquals(7, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        Assert.assertEquals(4, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        Assert.assertEquals(3, resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));

    }

    @Test
    public void createWriter() {
        Assert.assertTrue(writeOperation.createWriter(null) instanceof CouchbaseWriter);
    }

    @Test
    public void testGetSink() {
        Assert.assertEquals(sink, writeOperation.getSink());
    }
}
