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
package org.talend.components.api.component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.talend.components.api.AbstractTopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;

public abstract class AbstractComponentDefinition extends AbstractTopLevelDefinition implements ComponentDefinition {

    private ComponentConnector[] connectors;

    private ComponentTrigger[] triggers;

    public void setConnectors(ComponentConnector... conns) {
        this.connectors = conns;
    }

    public void setTriggers(ComponentTrigger... conns) {
        this.triggers = conns;
    }

    // protected Class<?> propertiesClass;

    @Override
    public String[] getFamilies() {
        // Subclass me
        return new String[] {};
    }

    @Override
    public ComponentConnector[] getConnectors() {
        return connectors;
    }

    @Override
    public ComponentTrigger[] getTriggers() {
        return triggers;
    }

    @Override
    protected String getI18nPrefix() {
        return "component."; //$NON-NLS-1$
    }

    @Override
    public ComponentProperties createProperties() {
        ComponentProperties compProp = instanciateComponentProperties();
        compProp.init();
        return compProp;
    }

    /**
     * DOC sgandon Comment method "instanciateComponentProperties".
     * 
     * @param compProp
     * @return
     */
    public ComponentProperties instanciateComponentProperties() {
        ComponentProperties compProp = null;
        try {
            Class<?> propertyClass = getPropertyClass();
            if (propertyClass == null) {
                return null;// TODO throw an exception
            } // else keep going
            Constructor c = propertyClass.getConstructor(String.class);
            compProp = (ComponentProperties) c.newInstance(new Object[] { "root" });
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
        return compProp;
    }

    @Override
    public ComponentProperties createRuntimeProperties() {
        ComponentProperties compProp = instanciateComponentProperties();
        compProp.initForRuntime();
        return compProp;
    }

    @Override
    public boolean supportsProperties(ComponentProperties properties) {
        return getPropertyClass().isAssignableFrom(properties.getClass());
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return false;
    }

    @Override
    public boolean isDataAutoPropagate() {
        return false;
    }

    @Override
    public boolean isConditionalInputs() {
        return false;
    }

    @Override
    public boolean isStartable() {
        return false;
    }

    public static final String AUTO = "Auto";

    public static final String NONE = "None";

    @Override
    public String getPartitioning() {
        return null;
    }

    @Override
    public String toString() {
        return getName() + " (" + getDisplayName() + ") - " + getTitle() //
                + "\n props: " + getPropertyClass();
    }

    abstract public Class<?> getPropertyClass();
}
