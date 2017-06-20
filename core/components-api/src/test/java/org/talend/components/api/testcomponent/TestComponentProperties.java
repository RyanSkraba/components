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
package org.talend.components.api.testcomponent;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newSchema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;

public class TestComponentProperties extends ComponentPropertiesImpl {

    private static final long serialVersionUID = 5751002825822282055L;

    public static final String USER_ID_PROP_NAME = "userId"; //$NON-NLS-1$

    public Form mainForm;

    public Property<Schema> mainOutput = newSchema("mainOutput");

    public Property<String> initLater = null;

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties(String name) {
        super(name);
    }

    public ValidationResult afterFormFinishMain(Repository<Properties> repo) {
        return new ValidationResult(Result.ERROR);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        initLater = newProperty("initLater");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        mainForm = Form.create(this, Form.MAIN);

        Form refForm = new Form(this, Form.REFERENCE);
        refForm.addRow(mainForm);
    }

    @Override
    public Schema getSchema(Connector connector, boolean isOutputConnection) {
        if (connector instanceof PropertyPathConnector) {
            Property<?> property = getValuedProperty(((PropertyPathConnector) connector).getPropertyPath());
            Object value = property.getValue();
            return value != null && Schema.class.isAssignableFrom(value.getClass()) ? (Schema) property.getValue() : null;
        } else {// not a connector handled by this class
            return null;
        }
    }

    @Override
    public Set<Connector> getAvailableConnectors(Set<? extends Connector> existingConnections, boolean isOutput) {
        HashSet<Connector> filteredConnectors = new HashSet<>(getAllConnectors());
        filteredConnectors.removeAll(existingConnections);
        return filteredConnectors;
    }

    public Set<? extends Connector> getAllConnectors() {
        return Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "mainOutput"));
    }

}
