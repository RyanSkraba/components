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

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;

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

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form moduleForm = Form.create(this, Form.MAIN);
        // Since this is a repeating property it has a list of values
        moduleForm.addRow(widget(selectedModuleNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(moduleForm);
    }

    // For the tests
    public SalesforceConnectionProperties getConnectionProps() {
        return connection;
    }

    public void beforeFormPresentMain() throws Exception {
        moduleNames = SalesforceSourceOrSink.getSchemaNames(null, this);
        selectedModuleNames.setPossibleValues(moduleNames);
        getForm(Form.MAIN).setAllowBack(true);
        getForm(Form.MAIN).setAllowFinish(true);
    }

    public ValidationResult afterFormFinishMain(Repository<Properties> repo) throws Exception {
        ValidationResult vr = SalesforceSourceOrSink.validateConnection(this);
        if (vr.getStatus() != ValidationResult.Result.OK) {
            return vr;
        }

        String connRepLocation = repo.storeProperties(connection, connection.name.getValue(), repositoryLocation, null);

        for (NamedThing nl : selectedModuleNames.getValue()) {
            String moduleId = nl.getName();
            SalesforceModuleProperties modProps = new SalesforceModuleProperties(moduleId);
            modProps.connection = connection;
            modProps.init();
            Schema schema = SalesforceSourceOrSink.getSchema(null, this, moduleId);
            modProps.moduleName.setValue(moduleId);
            modProps.main.schema.setValue(schema);
            repo.storeProperties(modProps, nl.getName(), connRepLocation, "main.schema");
        }
        return ValidationResult.OK;
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return connection;
    }
}
