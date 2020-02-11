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
package org.talend.components.jdbc.module;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.io.IOException;
import java.util.List;

import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.StringProperty;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * common database table selection properties
 *
 */
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

    public ValidationResult beforeTablename() throws IOException {
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(connection, "org.talend.components.jdbc.runtime.JDBCSource");
        try (SandboxedInstance sandboxI = RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(jdbcRuntimeInfo,
                connection.getClass().getClassLoader())) {
            SourceOrSink ss = (SourceOrSink) sandboxI.getInstance();
            ss.initialize(null, (ComponentProperties) connection);
            List<NamedThing> tablenames = ss.getSchemaNames(null);
            tablename.setPossibleNamedThingValues(tablenames);
        } catch (ComponentException ex) {
        	return new ValidationResult(ValidationResult.Result.ERROR, CommonUtils.getClearExceptionInfo(ex));
        }
        return ValidationResult.OK;
    }

    public void setConnection(RuntimeSettingProvider connection) {
        this.connection = connection;
    }

}
