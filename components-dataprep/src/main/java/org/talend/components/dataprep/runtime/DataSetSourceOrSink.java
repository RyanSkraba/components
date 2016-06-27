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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

abstract class DataSetSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -7131563944886842361L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetSourceOrSink.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(DataSetSink.class);

    protected RuntimeProperties runtimeProperties;

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler( //
                runtimeProperties.getUrl(), //
                runtimeProperties.getLogin(), //
                runtimeProperties.getPass(), //
                runtimeProperties.getDataSetId(), runtimeProperties.getDataSetName());
        if (DataPrepOutputModes.LiveDataset.equals(runtimeProperties.getMode())) {
            return ValidationResult.OK;
        }
        try {
            connectionHandler.validate();
        } catch (IOException e) {
            LOGGER.debug(messages.getMessage("error.sourceOrSinkValidationFailed", e));
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(messages.getMessage(e.getMessage()));
        }
        return ValidationResult.OK;
    }

    protected RuntimeProperties getRuntimeProperties() {
        return runtimeProperties;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }
}
