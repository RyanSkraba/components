// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow.runtime;

import java.util.Collection;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filterrow.processing.AndValueProcessor;
import org.talend.components.filterrow.processing.FilterDescriptor;
import org.talend.components.filterrow.processing.FiltersFactory;
import org.talend.components.filterrow.processing.LogicalOperator;
import org.talend.components.filterrow.processing.OrValueProcessor;
import org.talend.components.filterrow.processing.ValueProcessor;

/**
 * created by dmytro.chmyga on Dec 19, 2016
 */
public class TFilterRowWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = 8273263325653290191L;

    private final Sink sink;

    private final Collection<FilterDescriptor> filterDescriptors;

    private final Schema schemaFlow;

    private final Schema schemaReject;

    private final LogicalOperator logicalOperator;

    private ValueProcessor valueProcessor;

    public TFilterRowWriteOperation(Sink sink, Collection<FilterDescriptor> filterDescriptors, Schema schemaFlow,
            Schema schemaReject, LogicalOperator operator) {
        this.sink = sink;
        this.filterDescriptors = filterDescriptors;
        this.schemaFlow = schemaFlow;
        this.schemaReject = schemaReject;
        this.logicalOperator = operator;
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer arg0) {
        return new TFilterRowWriter(this, schemaFlow, schemaReject, valueProcessor);
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> arg0, RuntimeContainer arg1) {
        return Result.accumulateAndReturnMap(arg0);
    }

    @Override
    public Sink getSink() {
        return sink;
    }

    @Override
    public void initialize(RuntimeContainer arg0) {
        valueProcessor = logicalOperator == LogicalOperator.And ? new AndValueProcessor() : new OrValueProcessor();
        for (FilterDescriptor descriptor : filterDescriptors) {
            valueProcessor.addFilterForColumn(descriptor.getColumnName(), FiltersFactory.createFilter(descriptor));
        }
    }

}
