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
package org.talend.components.dataprep.runtime;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DataSetSink implements Sink {

    private static final long serialVersionUID = 3228265006313531905L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetSink.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(DataSetSink.class);

    RuntimeProperties runtimeProperties;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new DataSetWriteOperation(this);
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer, ComponentProperties componentProperties) {
        this.runtimeProperties = ((TDataSetOutputProperties) componentProperties).getRuntimeProperties();
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtimeContainer) {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler( //
                runtimeProperties.getUrl(), //
                runtimeProperties.getLogin(), //
                runtimeProperties.getPass(), //
                runtimeProperties.getDataSetName());
        if (DataPrepOutputModes.LiveDataset.equals(runtimeProperties.getMode())) {
            return ValidationResult.OK;
        }
        try {
            connectionHandler.validate();
        } catch (IOException e) {
            LOGGER.debug(messages.getMessage("error.validationFailed", e));
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(messages.getMessage("error.validationFailed", e));
        }
        return ValidationResult.OK;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }

    RuntimeProperties getRuntimeProperties() {
        return runtimeProperties;
    }
}