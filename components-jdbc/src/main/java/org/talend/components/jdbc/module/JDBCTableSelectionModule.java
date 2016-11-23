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

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.List;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.StringProperty;

public class JDBCTableSelectionModule extends PropertiesImpl {

    public StringProperty tablename = newString("tablename");

    private RuntimeSettingProvider connection = null;

    public JDBCTableSelectionModule(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form main = Form.create(this, Form.MAIN);
        main.addRow(widget(tablename).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(main);

        Form referForm = Form.create(this, Form.REFERENCE);
        referForm.addRow(widget(tablename).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE).setLongRunning(true));

        // TODO why do that?
        refreshLayout(referForm);
    }

    public ValidationResult beforeTablename() throws Exception {
        try {
            JDBCSourceOrSink ss = new JDBCSourceOrSink();
            ss.initialize(null, (ComponentProperties) connection);
            List<NamedThing> tablenames = ss.getSchemaNames(null);
            tablename.setPossibleNamedThingValues(tablenames);
        } catch (ComponentException ex) {
            return ex.getValidationResult();
        }
        return ValidationResult.OK;
    }

    public void setConnection(RuntimeSettingProvider connection) {
        this.connection = connection;
    }

}
