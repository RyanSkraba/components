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
package org.talend.components.jdbc.module;

import static org.talend.daikon.properties.presentation.Widget.*;

import org.talend.components.common.UserPasswordProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * common JDBC connection information properties
 *
 */
public class JDBCConnectionModule extends PropertiesImpl {

    public Property<String> jdbcUrl = PropertyFactory.newProperty("jdbcUrl").setRequired();

    // TODO use the right widget for it
    public DriverTable driverTable = new DriverTable("driverTable");

    public Property<String> driverClass = PropertyFactory.newProperty("driverClass").setRequired();

    public UserPasswordProperties userPassword = new UserPasswordProperties("userPassword");

    public JDBCConnectionModule(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        // TODO fix it
        // jdbcUrl.setValue("jdbc:");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form form = Form.create(this, Form.MAIN);
        form.addRow(jdbcUrl);
        form.addRow(widget(driverTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        form.addRow(driverClass);
        form.addRow(userPassword.getForm(Form.MAIN));
    }

}
