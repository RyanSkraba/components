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
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.salesforce.common.ExceptionUtil;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.sandbox.SandboxedInstance;

public class SalesforceModuleListProperties extends ComponentPropertiesImpl implements SalesforceProvideConnectionProperties {

    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection");

    private String repositoryLocation;

    private List<NamedThing> moduleNames;

    //
    // Properties
    //
    public Property<List<NamedThing>> selectedModuleNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedModuleNames"); //$NON-NLS-1$

    public SalesforceModuleListProperties(String name) {
        super(name);
    }

    public SalesforceModuleListProperties setConnection(SalesforceConnectionProperties connection) {
        this.connection = connection;
        return this;
    }

    public SalesforceModuleListProperties setRepositoryLocation(String location) {
        repositoryLocation = location;
        return this;
    }

    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form moduleForm = Form.create(this, Form.MAIN);
        // Since this is a repeating property it has a list of values
        moduleForm.addRow(widget(selectedModuleNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(moduleForm);
    }

    public void beforeFormPresentMain() throws Exception {
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS, USE_CURRENT_JVM_PROPS)) {
            SalesforceRuntimeSourceOrSink ss = (SalesforceRuntimeSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, this);
            ValidationResult vr = ss.validate(null);
            if (vr.getStatus() == ValidationResult.Result.OK) {
                try {
                    moduleNames = ss.getSchemaNames(null);
                } catch (Exception ex) {
                    throw new ComponentException(ExceptionUtil.exceptionToValidationResult(ex));
                }
                selectedModuleNames.setPossibleValues(moduleNames);
                getForm(Form.MAIN).setAllowBack(true);
                getForm(Form.MAIN).setAllowFinish(true);
            } else {
                throw new ComponentException(vr);
            }
        }
    }

    public ValidationResult afterFormFinishMain(Repository<Properties> repo) throws Exception {
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS, USE_CURRENT_JVM_PROPS)) {

            SalesforceRuntimeSourceOrSink ss = (SalesforceRuntimeSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, this);
            ValidationResult vr = ss.validate(null);
            if (vr.getStatus() != ValidationResult.Result.OK) {
                return vr;
            }

            String connRepLocation = repo.storeProperties(connection, connection.name.getValue(), repositoryLocation, null);

            for (NamedThing nl : selectedModuleNames.getValue()) {
                String moduleId = nl.getName();
                SalesforceModuleProperties modProps = new SalesforceModuleProperties(moduleId);
                modProps.connection = connection;
                modProps.init();
                Schema schema = ss.getEndpointSchema(null, moduleId);
                modProps.moduleName.setValue(moduleId);
                modProps.main.schema.setValue(schema);
                repo.storeProperties(modProps, nl.getName(), connRepLocation, "main.schema");
            }
            return ValidationResult.OK;
        }
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return connection;
    }
}
