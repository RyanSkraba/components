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

package org.talend.components.adapter.beam.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;

public class AssertResultWriteOperation implements WriteOperation<Result> {

    private AssertResultSink sink;

    public AssertResultWriteOperation(AssertResultSink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {

    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        Map<String, Object> results = Result.accumulateAndReturnMap(writerResults);
        List<String> expectedRows = Arrays.asList(sink.properties.data.getValue().split(sink.properties.rowDelimited.getValue()));
        Collections.sort(expectedRows);
        List<String> actualResult = (List<String>) results.get(AssertResultResult.ACTUAL_RESULT);
        if (actualResult != null) {
            Collections.sort(actualResult);
        }
        if (!expectedRows.equals(actualResult)) {
            throw new RuntimeException("\nExpected rows: " + expectedRows + "\nActual rows: " + actualResult);
        }
        return results;
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        return new AssertResultWriter(this);
    }

    @Override
    public AssertResultSink getSink() {
        return sink;
    }

}
