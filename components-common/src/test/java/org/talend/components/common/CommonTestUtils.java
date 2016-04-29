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
package org.talend.components.common;

import static org.hamcrest.Matchers.*;

import java.util.Set;

import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Property;

public class CommonTestUtils {

    static public void checkAllSchemaPathAreSchemaTypes(ComponentService service, ErrorCollector collector) {
        Set<ComponentDefinition> allComponents = service.getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            ComponentProperties properties = cd.createProperties();
            if (properties instanceof FixedConnectorsComponentProperties) {
                checkAllSchemaPathAreSchemaTypes((FixedConnectorsComponentProperties) properties, collector);
            }
        }
    }

    static public void checkAllSchemaPathAreSchemaTypes(FixedConnectorsComponentProperties fccp, ErrorCollector collector) {
        Set<PropertyPathConnector> allConnectors = fccp.getAllSchemaPropertiesConnectors(true);
        checkAllConnector(fccp, collector, allConnectors);
        allConnectors = fccp.getAllSchemaPropertiesConnectors(false);
        checkAllConnector(fccp, collector, allConnectors);
    }

    private static void checkAllConnector(FixedConnectorsComponentProperties fccp, ErrorCollector collector,
            Set<PropertyPathConnector> allConnectors) {
        for (PropertyPathConnector connector : allConnectors) {
            NamedThing property = fccp.getProperty(connector.getPropertyPath());
            collector.checkThat(property, notNullValue());
            collector.checkThat(property, anyOf(instanceOf(Property.class), instanceOf(SchemaProperties.class)));
        }
    }
}
