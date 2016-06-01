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

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.dataprep.connection.Column;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.components.dataprep.connection.DataPrepField;
import org.talend.components.dataprep.runtime.DataPrepAvroRegistry;
import org.talend.components.dataprep.runtime.RuntimeProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
public class TDataSetInputProperties extends FixedConnectorsComponentProperties {

    private static final Logger LOG = LoggerFactory.getLogger(TDataSetInputProperties.class);

    public SchemaProperties schema = new SchemaProperties("schema");

    public PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    public Property<String> url = PropertyFactory.newString("url");

    public Property<String> login = PropertyFactory.newString("login");

    public Property<String> pass = PropertyFactory.newString("pass");

    public Property<String> dataSetName = PropertyFactory.newString("dataSetName");

    public PresentationItem fetchSchema = new PresentationItem("fetchSchema", "FetchSchema");

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
        Form form = new Form(this, Form.MAIN);
        form.addRow(schema.getForm(Form.REFERENCE));
        form.addRow(url);
        form.addRow(login);
        form.addRow(Widget.widget(pass).setWidgetType(Widget.WidgetType.HIDDEN_TEXT));
        form.addRow(dataSetName);
        form.addRow(Widget.widget(fetchSchema).setWidgetType(Widget.WidgetType.BUTTON));
    }

    public ValidationResult afterFetchSchema() {
        if (!isRequiredFieldRight()) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage("All fields is required. You don't set one of them");
        } else {
            DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(url.getStringValue(),
                    login.getStringValue(), pass.getStringValue(), dataSetName.getStringValue());
            List<Column> columnList = null;
            boolean wasProblem = false;
            ValidationResult validationResult = ValidationResult.OK;
            try {
                connectionHandler.connect();
                columnList = connectionHandler.readSourceSchema();
            } catch (IOException e) {
                LOG.debug("Dataprep fetch schema error.", e);
                wasProblem = true;
                validationResult = new ValidationResult().setStatus(Result.ERROR).setMessage(e.getMessage());
            } finally {
                try {
                    connectionHandler.logout();
                } catch (IOException e) {
                    LOG.debug("Failed to logout to Dataprep server: {}", e);
                    wasProblem = true;
                    validationResult = new ValidationResult().setStatus(Result.ERROR).setMessage(e.getMessage());
                }
            }

            if (wasProblem) {
                return validationResult;
            }

            DataPrepField[] scemaRow = new DataPrepField[columnList.size()];
            int i = 0;
            for (Column column : columnList) {
                scemaRow[i] = new DataPrepField(column.getName(), column.getType(), null);
                i++;
            }
            AvroRegistry avroRegistry = DataPrepAvroRegistry.getDataPrepInstance();
            schema.schema.setValue(avroRegistry.inferSchema(scemaRow));

            return validationResult;
        }
    }

    private boolean isRequiredFieldRight() {
        if (isNotNullAndNotEmpty(url.getStringValue())) {
            return false;
        }
        if (isNotNullAndNotEmpty(login.getStringValue())) {
            return false;
        }
        if (isNotNullAndNotEmpty(pass.getStringValue())) {
            return false;
        }
        if (isNotNullAndNotEmpty(dataSetName.getStringValue())) {
            return false;
        }
        return true;
    }

    private boolean isNotNullAndNotEmpty(String propertyStringValue) {
        return propertyStringValue == null || propertyStringValue.isEmpty();
    }

    public RuntimeProperties getRuntimeProperties() {
        RuntimeProperties runtimeProperties = new RuntimeProperties();
        runtimeProperties.setUrl(url.getStringValue());
        runtimeProperties.setLogin(login.getStringValue());
        runtimeProperties.setPass(pass.getStringValue());
        runtimeProperties.setDataSetName(dataSetName.getStringValue());
        runtimeProperties.setSchema(schema.schema.getStringValue());
        return runtimeProperties;
    }

    public Schema getSchema() {
        return schema.schema.getValue();
    }
}
