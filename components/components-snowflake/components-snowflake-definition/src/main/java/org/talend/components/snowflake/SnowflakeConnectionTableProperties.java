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
package org.talend.components.snowflake;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.daikon.properties.presentation.Form;

public abstract class SnowflakeConnectionTableProperties extends FixedConnectorsComponentProperties
        implements SnowflakeProvideConnectionProperties {

    // Collections
    //
    public SnowflakeConnectionProperties connection = new SnowflakeConnectionProperties("connection"); //$NON-NLS-1$

    public SnowflakeTableProperties table;

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "table.main");

    public SnowflakeConnectionTableProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        table = new SnowflakeTableProperties("table");
        table.connection = getConnectionProperties();
        table.setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                afterMainSchema();
            }
        });
    }

    public Schema getSchema() {
        return table.main.schema.getValue();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(table.getForm(Form.REFERENCE));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(connection.getForm(Form.ADVANCED));
    }

    /**
     * This methods serves to update reject schema or/and schema flow after main schema(table.main.schema) was changed.
     */
    public void afterMainSchema() {
        // Implement in subclasses.
    };


    public String getTableName() {
        return table.tableName.getValue();
    }

    @Override
    public SnowflakeConnectionProperties getConnectionProperties() {
        return connection;
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

}
