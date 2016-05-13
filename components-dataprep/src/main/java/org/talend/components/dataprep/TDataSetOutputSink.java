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
package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TDataSetOutputSink implements Sink {

    TDataSetOutputProperties properties;

    @Override
    public WriteOperation<?> createWriteOperation() {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(properties.url.getStringValue(),
                properties.login.getStringValue(), properties.pass.getStringValue(),
                properties.dataSetName.getStringValue());
        return new TDataSetWriteOperation(this, connectionHandler, properties.limit.getIntValue());
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer, ComponentProperties componentProperties) {
        this.properties = (TDataSetOutputProperties) componentProperties;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtimeContainer) {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(properties.url.getStringValue(),
                properties.login.getStringValue(), properties.pass.getStringValue(),
                properties.dataSetName.getStringValue());
        boolean validate;
        try {
            validate = connectionHandler.validate();
        } catch (IOException e) {
            validate = false;
        }
        if (validate)
            return ValidationResult.OK;
        else
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }
}