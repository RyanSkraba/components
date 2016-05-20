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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TDataSetOutputSink implements Sink {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepConnectionHandler.class);

    RuntimeProperties runtimeProperties;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new TDataSetWriteOperation(this, runtimeProperties);
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer, ComponentProperties componentProperties) {
        this.runtimeProperties = ((TDataSetOutputProperties) componentProperties).getRuntimeProperties();
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtimeContainer) {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler( //
                runtimeProperties.getUlr(), //
                runtimeProperties.getLogin(), //
                runtimeProperties.getPass(), //
                runtimeProperties.getDataSetName());
            if (DataPrepOutputModes.LIVEDATASET.equals(runtimeProperties.getMode())) {
                return ValidationResult.OK;
            }
            try {
                connectionHandler.validate();
            } catch (IOException e) {
                LOGGER.debug("Validation isn't passed. Reason: {}", e);
                return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                        .setMessage(e.getMessage());
            }
        return ValidationResult.OK;
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