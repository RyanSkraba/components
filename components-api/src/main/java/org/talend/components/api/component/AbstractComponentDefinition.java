package org.talend.components.api.component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.talend.components.api.AbstractTopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;

public abstract class AbstractComponentDefinition extends AbstractTopLevelDefinition implements ComponentDefinition {

    private ComponentConnector[] connectors;

    public void setConnectors(ComponentConnector... conns) {
        this.connectors = conns;
    }

    protected Class<?> propertiesClass;

    public String[] getFamilies() {
        // Subclass me
        return new String[] {};
    }

    @Override
    public ComponentConnector[] getConnectors() {
        return connectors;
    }

    @Override
    protected String getI18nPrefix() {
        return "component."; //$NON-NLS-1$
    }

    @Override
    public ComponentProperties createProperties() {
        ComponentProperties compProp = null;
        try {
            Constructor c = propertiesClass.getConstructor(String.class);
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
        compProp.init();
        return compProp;
    }

    public boolean supportsProperties(ComponentProperties properties) {
        return propertiesClass.isAssignableFrom(properties.getClass());
    }

    public boolean isSchemaAutoPropagate() {
        return false;
    }

    public boolean isDataAutoPropagate() {
        return false;
    }

    public boolean isConditionalInputs() {
        return false;
    }

    public boolean isStartable() {
        return false;
    }

    public static final String AUTO = "Auto";

    public static final String NONE = "None";

    public String getPartitioning() {
        return null;
    }

    public String toString() {
        return getName() + " (" + getDisplayName() + ") - " + getTitle() //
                + "\n props: " + propertiesClass;
    }

}
