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
package org.talend.components.api.service.common.testcomponent;

import static org.talend.daikon.properties.property.PropertyFactory.newSchema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.property.Property;

public class TestComponentProperties extends ComponentPropertiesImpl {

    public TestComponentProperties(String name) {
        super(name);
    }

    public Property<Schema> mainOutput = newSchema("mainOutput");

    public Set<? extends Connector> getAllConnectors() {
        return Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "mainOutput"));
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

}
