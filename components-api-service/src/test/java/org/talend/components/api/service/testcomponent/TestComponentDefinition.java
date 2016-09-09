// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.service.testcomponent;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.Set;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.StringProperty;

import aQute.bnd.annotation.component.Component;

public class TestComponentDefinition extends AbstractComponentDefinition implements ComponentDefinition {

    public static final String COMPONENT_NAME = "TestComponent"; //$NON-NLS-1$

    public TestComponentDefinition() {
        super(COMPONENT_NAME);
    }

    protected TestComponentProperties properties;

    @Override
    public String[] getFamilies() {
        return new String[] { "level1/level2", "newlevel1/newlevel2" };
    }

    @Override
    public Property[] getReturnProperties() {
        StringProperty return1 = newProperty("return1");
        return1.setI18nMessageFormatter(getI18nMessageFormatter());
        return new Property[] { return1, RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP,
                RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_REJECT_RECORD_COUNT_PROP };
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        return "testCompIcon_32x32.png";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TestComponentProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { NestedComponentProperties.class, ComponentPropertiesWithDefinedI18N.class,
                InheritedComponentProperties.class };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology compType) {
        return new SimpleRuntimeInfo(this.getClass().getClassLoader(), "org.talend.components.api.test", "test-components",
                COMPONENT_NAME);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return null;
    }

}
