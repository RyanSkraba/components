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
package org.talend.components.processing.runtime.limit;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.talend.components.processing.definition.limit.LimitProperties;

/**
 * Limit {@link DoFn} used to limit the number of records processed from the source
 * {@link org.apache.beam.sdk.values.PCollection}. At the time of writing, this {@link DoFn} uses a static counter
 * defined at the {@link LimitRuntime} {@link org.apache.beam.sdk.transforms.PTransform} level, which makes it unusable
 * in a distributed mode. Consequently, it can be used only with local runners.
 */
public class LimitDoFn extends DoFn<IndexedRecord, IndexedRecord> {

    private LimitProperties properties = null;

    @ProcessElement
    public void processElement(ProcessContext context) {
        if (LimitRuntime.counter.getAndIncrement() < properties.limit.getValue()) {
            context.output(context.element());
        }
    }

    public LimitDoFn withProperties(LimitProperties properties) {
        this.properties = properties;
        return this;
    }
}
