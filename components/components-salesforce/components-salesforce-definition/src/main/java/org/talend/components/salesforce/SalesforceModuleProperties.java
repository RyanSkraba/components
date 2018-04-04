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
package org.talend.components.salesforce;

import static org.talend.components.salesforce.SalesforceDefinition.SOURCE_OR_SINK_CLASS;
import static org.talend.components.salesforce.SalesforceDefinition.USE_CURRENT_JVM_PROPS;
import static org.talend.components.salesforce.SalesforceDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.common.ExceptionUtil;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.StringProperty;
import org.talend.daikon.sandbox.SandboxedInstance;

public class SalesforceModuleProperties extends ComponentPropertiesImpl implements SalesforceProvideConnectionProperties {

    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection");

    //
    // Properties
    //
    public StringProperty moduleName = newString("moduleName"); //$NON-NLS-1$

    public ISchemaListener schemaListener;

    public SchemaProperties main = new SchemaProperties("main") {

        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }
    };

    public SalesforceModuleProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form moduleForm = Form.create(this, Form.MAIN);
        moduleForm.addRow(widget(moduleName).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(moduleForm);

        Form moduleRefForm = Form.create(this, Form.REFERENCE);
        moduleRefForm.addRow(widget(moduleName).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE).setLongRunning(true));

        moduleRefForm.addRow(main.getForm(Form.REFERENCE));
        refreshLayout(moduleRefForm);
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    // consider beforeActivate and beforeRender (change after to afterActivate)

    public ValidationResult beforeModuleName() throws Exception {
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS, USE_CURRENT_JVM_PROPS)) {
            SalesforceRuntimeSourceOrSink ss = (SalesforceRuntimeSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, getEffectiveConnection());
            ValidationResult vr = ss.validate(null);
            if (vr.getStatus() == ValidationResult.Result.OK) {
                try {
                    List<NamedThing> moduleNames = ss.getSchemaNames(null);
                    moduleName.setPossibleNamedThingValues(moduleNames);
                } catch (Exception ex) {
                    return ExceptionUtil.exceptionToValidationResult(ex);
                }
            } else {
                return vr;
            }

            return ValidationResult.OK;
        }
    }

    public ValidationResult afterModuleName() throws Exception {
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS, USE_CURRENT_JVM_PROPS)) {
            SalesforceRuntimeSourceOrSink ss = (SalesforceRuntimeSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, getEffectiveConnection());
            ValidationResult vr = ss.validate(null);
            if (vr.getStatus() == ValidationResult.Result.OK) {
                try {
                    Schema schema = ss.getEndpointSchema(null, moduleName.getStringValue());
                    main.schema.setValue(schema);
                    moduleName.setPossibleValues(Collections.emptyList());
                } catch (Exception ex) {
                    throw new ComponentException(ExceptionUtil.exceptionToValidationResult(ex));
                }
            } else {
                throw new ComponentException(vr);
            }

            return ValidationResult.OK;
        }
    }

    private SalesforceConnectionProperties getEffectiveConnection() {
        if (getConnectionProperties().getReferencedConnectionProperties() != null) {
            return getConnectionProperties().getReferencedConnectionProperties();
        }

        return getConnectionProperties();
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return connection;
    }
}
