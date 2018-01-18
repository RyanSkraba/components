// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.tmarklogicinput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.marklogic.MarkLogicProvideConnectionProperties;
import org.talend.components.marklogic.data.MarkLogicDatasetProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class MarkLogicInputProperties extends FixedConnectorsComponentProperties implements MarkLogicProvideConnectionProperties {

    public MarkLogicConnectionProperties connection = new MarkLogicConnectionProperties("connection");

    public MarkLogicDatasetProperties datasetProperties = new MarkLogicDatasetProperties("datasetProperties");

    public SchemaProperties inputSchema = new SchemaProperties("inputSchema"){
        public void afterSchema() {
            if (inputSchemaListener != null) {
                inputSchemaListener.afterSchema();
            }
        }
    };

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "datasetProperties.main");
    protected transient PropertyPathConnector INCOMING_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "inputSchema");

    public Property<Boolean> criteriaSearch = PropertyFactory.newBoolean("criteriaSearch");
    public Property<String> docIdColumn =  PropertyFactory.newString("docIdColumn");

    public Property<Integer> maxRetrieve = PropertyFactory.newInteger("maxRetrieve");

    private ISchemaListener inputSchemaListener;

    public MarkLogicInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        datasetProperties.setDatastoreProperties(connection);
        criteriaSearch.setRequired();
        criteriaSearch.setValue(true);
        docIdColumn.setRequired();
        maxRetrieve.setValue(-1);

        if (isOutputSchemaInvalid()) {
            datasetProperties.setupSchema();
        }

        setInputSchemaListener(new ISchemaListener() { //TODO replace with lambda

            @Override
            public void afterSchema() {
                updateDocIdColumnPossibleValues();
                if (isOutputSchemaInvalid()) {
                    datasetProperties.setupSchema();
                }
            }
        });
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN) && connection != null) {
            for (Form childForm : connection.getForms()) {
                connection.refreshLayout(childForm);
            }
            if (!isPlainOutputConnectionMode()) {
                updateDocIdColumnPossibleValues();
            }

            datasetProperties.getForm(Form.MAIN).getWidget(datasetProperties.criteria).setVisible(isPlainOutputConnectionMode());
            form.getWidget(docIdColumn).setHidden(isPlainOutputConnectionMode());
        }

        if (form.getName().equals(Form.ADVANCED)) {
            boolean isCriteriaModeUsed = isPlainOutputConnectionMode();

            form.getWidget(maxRetrieve).setVisible(isCriteriaModeUsed);
            datasetProperties.getForm(Form.ADVANCED).setVisible(isCriteriaModeUsed);
            if (isCriteriaModeUsed) {
                datasetProperties.refreshLayout(datasetProperties.getForm(Form.ADVANCED));
            }
        }
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form referenceForm = new Form(this, Form.REFERENCE);
        referenceForm.addRow(datasetProperties.getForm(Form.REFERENCE));
        referenceForm.getWidget(datasetProperties).setVisible();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(referenceForm);
        mainForm.addRow(inputSchema.getForm(Form.REFERENCE));
        mainForm.getWidget(inputSchema).setHidden();
        Form inputSchemaForm = ((Form) mainForm.getWidget(inputSchema).getContent());
        ((Property)inputSchemaForm.getWidget(inputSchema.schema).getContent()).removeFlag(Property.Flags.HIDDEN);
        mainForm.addRow(criteriaSearch);
        mainForm.addRow(datasetProperties.getForm(Form.MAIN));
        mainForm.addColumn(widget(docIdColumn).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(maxRetrieve);
        advancedForm.addRow(datasetProperties.getForm(Form.ADVANCED));
    }

    /**
     *
     * @return true when in output schema first column name is not 'docId'
     */
    private boolean isOutputSchemaInvalid() {
        return datasetProperties.main.schema.getValue() == null || //NPE check
                datasetProperties.main.schema.getValue().getFields().isEmpty() || //empty schema is invalid
                !"docId".equals(datasetProperties.main.schema.getValue().getFields().get(0).name()); //not valid 'schema header'. Should be docId
    }

    public void afterCriteriaSearch() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.singleton(INCOMING_CONNECTOR);
        }
    }

    @Override
    public MarkLogicConnectionProperties getConnectionProperties() {
        return connection;
    }

    private boolean isPlainOutputConnectionMode() {
        return criteriaSearch.getValue();
    }

    private void updateDocIdColumnPossibleValues() {
        List<String> inputFields = new ArrayList<>();
        for (Schema.Field inputField: inputSchema.schema.getValue().getFields()) {
            inputFields.add(inputField.name());
        }
        docIdColumn.setPossibleValues(inputFields);
    }

    public void setInputSchemaListener(ISchemaListener schemaListener) {
        this.inputSchemaListener = schemaListener;
    }
}
