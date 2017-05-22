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
package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class FileDelimitedSink extends FileSourceOrSink implements Sink {

    @Override
    public FileDelimitedWriteOperation createWriteOperation() {
        return new FileDelimitedWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validate = super.validate(container);
        // also check that the properties is the right type
        if (validate.getStatus() != ValidationResult.Result.ERROR) {
            if (!(properties instanceof TFileOutputDelimitedProperties)) {
                return new ValidationResult(ValidationResult.Result.ERROR,
                        "properties should be of type :" + TFileOutputDelimitedProperties.class.getCanonicalName());
            }
        }
        return validate;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }

    public TFileOutputDelimitedProperties getOutputProperties() {
        return (TFileOutputDelimitedProperties) properties;
    }
}
