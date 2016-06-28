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
package org.talend.components.dataprep.tdatasetinput;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.talend.daikon.properties.property.Property.Flags.DESIGN_TIME_ONLY;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.dataprep.DataPrepProperties;
import org.talend.components.dataprep.connection.Column;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.components.dataprep.connection.DataPrepField;
import org.talend.components.dataprep.runtime.DataPrepAvroRegistry;
import org.talend.components.dataprep.runtime.RuntimeProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * The ComponentProperties subclass provided by a component stores the configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is provided at design-time to configure a
 * component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the properties to the user.</li>
 * </ol>
 * 
 * The TDataSetInputProperties has two properties:
 * <ol>
 * <li>{code filename}, a simple property which is a String containing the file path that this component will read.</li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class TDataSetInputProperties extends DataPrepProperties {

    private static final Logger LOG = LoggerFactory.getLogger(TDataSetInputProperties.class);

    public Property<String> dataSetId = PropertyFactory.newString("dataSetId").setRequired();

    public final Property<String> dataSetName = PropertyFactory.newString("dataSetName").setRequired()
            .setFlags(EnumSet.of(DESIGN_TIME_ONLY));

    public final PresentationItem fetchSchema = new PresentationItem("fetchSchema", "FetchSchema");

    public TDataSetInputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(mainConnector);
        }
        return Collections.emptySet();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = getForm(Form.MAIN);
        form.addRow(Widget.widget(this.dataSetName).setWidgetType("widget.type.dataset.selection"));
        form.addRow(Widget.widget(fetchSchema).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(dataSetId);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    public ValidationResult afterFetchSchema() {
        if (!isRequiredFieldRight()) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(getI18nMessage("error.allFieldsIsRequired"));
        } else {
            DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(url.getStringValue(),
                    login.getStringValue(), pass.getStringValue(), dataSetId.getStringValue(), dataSetName.getStringValue());
            List<Column> columnList = null;
            boolean wasProblem = false;
            ValidationResult validationResult = ValidationResult.OK;
            try {
                connectionHandler.connect();
                columnList = connectionHandler.readSourceSchema();
            } catch (IOException e) {
                LOG.debug(getI18nMessage("error.schemaIsNotFetched", e));
                wasProblem = true;
                validationResult = new ValidationResult().setStatus(Result.ERROR)
                        .setMessage(getI18nMessage("error.schemaIsNotFetched", e.getMessage()));
            } finally {
                try {
                    connectionHandler.logout();
                } catch (IOException e) {
                    LOG.debug(getI18nMessage("error.failedToLogout", e));
                    wasProblem = true;
                    validationResult = new ValidationResult().setStatus(Result.ERROR)
                            .setMessage(getI18nMessage("error.failedToLogout", e.getMessage()));
                }
            }

            if (wasProblem) {
                return validationResult;
            }

            DataPrepField[] schemaRow = new DataPrepField[columnList.size()];
            int i = 0;
            for (Column column : columnList) {
                schemaRow[i] = new DataPrepField(column.getName(), column.getType(), null);
                i++;
            }
            AvroRegistry avroRegistry = DataPrepAvroRegistry.getDataPrepInstance();
            schema.schema.setValue(avroRegistry.inferSchema(schemaRow));

            return validationResult;
        }
    }

    private boolean isRequiredFieldRight() {
        return !isEmpty(url.getStringValue()) && !isEmpty(login.getStringValue()) && !isEmpty(pass.getStringValue())
                && !isEmpty(dataSetName.getStringValue()) && !isEmpty(dataSetId.getStringValue());
    }

    public RuntimeProperties getRuntimeProperties() {
        RuntimeProperties runtimeProperties = new RuntimeProperties();
        runtimeProperties.setUrl(url.getStringValue());
        runtimeProperties.setLogin(login.getStringValue());
        runtimeProperties.setPass(pass.getStringValue());
        runtimeProperties.setDataSetName(dataSetName.getStringValue());
        runtimeProperties.setDataSetId(dataSetId.getStringValue());
        runtimeProperties.setSchema(schema.schema.getStringValue());
        return runtimeProperties;
    }
}
