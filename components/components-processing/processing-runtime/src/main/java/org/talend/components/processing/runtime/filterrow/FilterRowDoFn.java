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
package org.talend.components.processing.runtime.filterrow;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.talend.components.processing.definition.filterrow.FilterRowProperties;

/**
 * This Beam function is exclusively used when a FilterRow requires both a main and reject output. If only one or the other is
 * required, the {@link org.talend.components.processing.runtime.filterrow.FilterRowPredicate} should be used directly to avoid
 * creating extra outputs.
 */
public class FilterRowDoFn extends DoFn<IndexedRecord, IndexedRecord> {

    private final FilterRowPredicate predicate;

    public FilterRowDoFn(FilterRowProperties properties) {
        this.predicate = new FilterRowPredicate(properties);
    }

    @ProcessElement
    public void processElement(ProcessContext context) {
        IndexedRecord record = context.element();
        if (predicate.apply(record)) {
            context.output(record);
        } else {
            context.output(FilterRowRuntime.rejectOutput, record);
        }
    }
}
