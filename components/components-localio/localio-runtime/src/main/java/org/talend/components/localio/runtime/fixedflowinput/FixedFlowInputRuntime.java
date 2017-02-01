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
package org.talend.components.localio.runtime.fixedflowinput;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.localio.fixedflowinput.FixedFlowInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class FixedFlowInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>> implements
        RuntimableRuntime<FixedFlowInputProperties> {

    private FixedFlowInputProperties properties;

    public ValidationResult initialize(RuntimeContainer container, FixedFlowInputProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin begin) {
        return begin.apply(properties.getName(), Read.from(new FixedFlowInputBoundedSource() //
                .withSchema(properties.schemaFlow.schema.getValue())//
                .withValues(properties.values.getValue()) //
                .withNbRows(properties.nbRows.getValue())));
    }
}
