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

import static org.talend.components.snowflake.SnowflakeDefinition.SOURCE_OR_SINK_CLASS;
import static org.talend.components.snowflake.SnowflakeDefinition.USE_CURRENT_JVM_PROPS;
import static org.talend.components.snowflake.SnowflakeDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Collections;
import java.util.List;

import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.StringProperty;
import org.talend.daikon.sandbox.SandboxedInstance;

public class SnowflakeTableProperties extends ComponentPropertiesImpl implements SnowflakeProvideConnectionProperties {

    public SnowflakeConnectionProperties connection;

    //
    // Properties
    //
    public StringProperty tableName = newString("tableName"); //$NON-NLS-1$

    public ISchemaListener schemaListener;

    public SchemaProperties main = new SchemaProperties("main") {

        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }
    };

    public SnowflakeTableProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form tableForm = Form.create(this, Form.MAIN);

        tableForm.addRow(widget(tableName).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(tableForm);

        Form tableRefForm = Form.create(this, Form.REFERENCE);
        tableRefForm.addRow(widget(tableName).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE).setLongRunning(true));

        tableRefForm.addRow(main.getForm(Form.REFERENCE));
        refreshLayout(tableRefForm);
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    public ValidationResult beforeTableName() throws Exception {
        ValidationResultMutable vr = new ValidationResultMutable();
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS, USE_CURRENT_JVM_PROPS)) {
            SnowflakeRuntimeSourceOrSink ss = (SnowflakeRuntimeSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, connection);
            List<NamedThing> tableNames = ss.getSchemaNames(null);
            tableName.setPossibleNamedThingValues(tableNames);
        } catch (Exception ex) {
            vr.setMessage(ex.getMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
        }
        return vr;
    }

    public ValidationResult afterTableName() throws Exception {
        ValidationResultMutable vr = new ValidationResultMutable();
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS, USE_CURRENT_JVM_PROPS)) {
            SnowflakeRuntimeSourceOrSink ss = (SnowflakeRuntimeSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, connection);
            main.schema.setValue(ss.getEndpointSchema(null, tableName.getValue()));
            tableName.setPossibleValues(Collections.emptyList());
        } catch (Exception ex) {
            vr.setMessage(ex.getMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
        }
        return vr;
    }

    @Override
    public SnowflakeConnectionProperties getConnectionProperties() {
        return connection;
    }

}
