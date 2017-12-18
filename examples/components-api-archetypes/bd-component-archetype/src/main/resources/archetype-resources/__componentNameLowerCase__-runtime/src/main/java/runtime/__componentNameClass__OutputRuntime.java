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
package ${package}.runtime;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.avro.generic.IndexedRecord;

import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import ${packageTalend}.api.component.runtime.RuntimableRuntime;
import ${packageTalend}.api.container.RuntimeContainer;
import ${packageTalend}.${componentNameLowerCase}.definition.output.${componentNameClass}OutputProperties;
import ${packageDaikon}.properties.ValidationResult;

public class ${componentNameClass}OutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone> implements
        RuntimableRuntime<${componentNameClass}OutputProperties> {

    /**
     * The component instance that this runtime is configured for.
     */
    private ${componentNameClass}OutputProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ${componentNameClass}OutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        return null;
    }
}
