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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.talend.components.api.AbstractTopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;

public abstract class AbstractComponentDefinition extends AbstractTopLevelDefinition implements ComponentDefinition {

    private Connector[] connectors;

    private Trigger[] triggers;

    public void setConnectors(Connector... conns) {
        this.connectors = conns;
    }

    public void setTriggers(Trigger... conns) {
        this.triggers = conns;
    }

    // protected Class<?> propertiesClass;

    @Override
    public String[] getFamilies() {
        // Subclass me
        return new String[]{};
    }

    @Override
    public Connector[] getConnectors() {
        return connectors;
    }

    @Override
    public Trigger[] getTriggers() {
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
            Constructor<?> c = propertyClass.getConstructor(String.class);
            compProp = (ComponentProperties) c.newInstance(new Object[]{"root"});
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
    public boolean supportsProperties(ComponentProperties... properties) {
        // compute all supported classes
        Class<? extends ComponentProperties>[] supportedNestedClasses = getNestedCompatibleComponentPropertiesClass();
        List<Class<? extends ComponentProperties>> supportedClasses = new ArrayList(supportedNestedClasses.length);
        Collections.addAll(supportedClasses, supportedNestedClasses);
        supportedClasses.add(getPropertyClass());
        // create a list of Classes to check.
        List<Class<? extends ComponentProperties>> classesToCheck = new ArrayList<>(properties.length);
        for (ComponentProperties cp : properties) {
            classesToCheck.add(cp.getClass());
        }
        return supportedClasses.containsAll(classesToCheck);
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

    //FIXME use InputComponenetDefinition
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

    /**
     * @return the associated ComponentProperties class associated with the Component. This shall be used to initialised
     * the runtime classes.
     */
    abstract public Class<? extends ComponentProperties> getPropertyClass();

    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return (Class<? extends ComponentProperties>[]) Array.newInstance(Class.class, 0);
    }


    public Class<? extends ComponentProperties>[] concatPropertiesClasses(Class<? extends ComponentProperties>[] first,
                                                                          Class<? extends ComponentProperties>[] second) {
        Class<? extends ComponentProperties>[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
