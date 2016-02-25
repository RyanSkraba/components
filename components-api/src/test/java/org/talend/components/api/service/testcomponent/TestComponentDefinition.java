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

import java.io.InputStream;
import java.util.Map;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + TestComponentDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TestComponentDefinition extends AbstractComponentDefinition implements ComponentDefinition {

    public static final String COMPONENT_NAME = "TestComponent"; //$NON-NLS-1$

    public TestComponentDefinition() {
        setConnectors(new Connector(ConnectorType.FLOW, 0, 0));
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 0), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    protected TestComponentProperties properties;

    @Override
    public ComponentRuntime createRuntime() {
        return new ComponentRuntime() {

            @Override
            public void outputMain(Map<String, Object> row) throws Exception {
                // TODO Auto-generated method stub

            }

            @Override
            public void outputEnd() throws Exception {
                // TODO Auto-generated method stub

            }

            @Override
            public void outputBegin(ComponentProperties props) throws Exception {
                // TODO Auto-generated method stub

            }

            @Override
            public Map<String, Object> inputRow() throws Exception {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void inputEnd() throws Exception {
                // TODO Auto-generated method stub

            }

            @Override
            public void inputBegin(ComponentProperties props) throws Exception {
                // TODO Auto-generated method stub

            }
        };
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "level1/level2", "newlevel1/newlevel2" };
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        return "testCompIcon_32x32.png";
    }

    @Override
    public InputStream getMavenPom() {
        return this.getClass().getResourceAsStream("pom.xml");
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

}
