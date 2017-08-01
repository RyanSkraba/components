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
package org.talend.components.marketo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.properties.presentation.Form;

public abstract class MarketoComponentProperties extends FixedConnectorsComponentProperties
        implements MarketoProvideConnectionProperties {

    public TMarketoConnectionProperties connection = new TMarketoConnectionProperties("connection");

    /**/
    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaInput");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public ISchemaListener schemaListener;

    public SchemaProperties schemaInput = new SchemaProperties("schemaInput") {

        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }
    };

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject");

    private static final long serialVersionUID = 5587867978797981L;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoComponentProperties.class);

    public MarketoComponentProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(schemaInput.getForm(Form.REFERENCE));
        // Advanced
        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(connection.getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    public List<String> getSchemaFields() {
        return MarketoUtils.getSchemaFields(schemaInput.schema.getValue());
    }

    @Override
    public TMarketoConnectionProperties getConnectionProperties() {
        return connection.getConnectionProperties();
    }

    public Boolean isApiSOAP() {
        return APIMode.SOAP.equals(getConnectionProperties().apiMode.getValue());
    }

    public Boolean isApiREST() {
        return APIMode.REST.equals(getConnectionProperties().apiMode.getValue());
    }

}
