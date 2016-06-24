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

import java.util.EnumSet;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public abstract class DataPrepProperties extends FixedConnectorsComponentProperties {

    private static final Logger LOG = LoggerFactory.getLogger(DataPrepProperties.class);

    public final SchemaProperties schema = new SchemaProperties("schema");

    public final PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    public final Property<String> url = PropertyFactory.newString("url").setRequired();

    public Property<String> login = PropertyFactory.newString("login").setRequired();

    public Property<String> pass = PropertyFactory.newString("pass").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public DataPrepProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = new Form(this, Form.MAIN);
        form.addRow(schema.getForm(Form.REFERENCE));
        form.addRow(url);
        form.addRow(login);
        form.addRow(Widget.widget(pass).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

    public Schema getSchema() {
        return schema.schema.getValue();
    }
}