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
package org.talend.components.localio.runtime.devnull;

import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.talend.components.adapter.beam.io.rowgenerator.RowGeneratorIO;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.localio.devnull.DevNullOutputProperties;
import org.talend.components.localio.fixed.FixedInputProperties;
import org.talend.components.localio.runtime.fixed.FixedDatasetRuntime;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

public class DevNullOutputRuntime extends PTransform<PCollection<IndexedRecord>, PCollection<IndexedRecord>>
        implements RuntimableRuntime<DevNullOutputProperties> {

    private DevNullOutputProperties properties;

    public ValidationResult initialize(RuntimeContainer container, DevNullOutputProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PCollection in) {
        return in;
    }
}
