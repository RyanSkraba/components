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
package org.talend.components.api.component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.talend.components.api.AbstractTopLevelDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.property.Property;

public abstract class AbstractComponentDefinition extends AbstractTopLevelDefinition implements ComponentDefinition {

    /**
     * Component name. It is used to define component name, which will be displayed in Studio Palette Also it is used by
     * bnd library to create OSGi bundle
     */
    private String componentName;

    private final Set<ExecutionEngine> engines;

    /**
     * Constructor sets component name
     *
     * @param componentName component name
     * @param allEngines true if available for all execution engines, false if not available for none (this should hide the component).
     */
    public AbstractComponentDefinition(String componentName, boolean allEngines) {
        this.componentName = componentName;
        this.engines = allEngines ? EnumSet.allOf(ExecutionEngine.class) : EnumSet.noneOf(ExecutionEngine.class);
        setupI18N(new Property<?>[] { RETURN_ERROR_MESSAGE_PROP, RETURN_REJECT_RECORD_COUNT_PROP,
                RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_TOTAL_RECORD_COUNT_PROP });
    }

    /**
     * Constructor sets component name
     * 
     * @param componentName component name
     * @param engine1 an execution engine that this component is valid for.
     * @param engineOthers other execution engines that this component can generate runtimes for. (This technique with
     * varargs is just to guarantee that there is at least one execution engine specified.)
     */
    public AbstractComponentDefinition(String componentName, ExecutionEngine engine1, ExecutionEngine... engineOthers) {
        this.componentName = componentName;
        this.engines = EnumSet.of(engine1, engineOthers);
        setupI18N(new Property<?>[] { RETURN_ERROR_MESSAGE_PROP, RETURN_REJECT_RECORD_COUNT_PROP,
                RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_TOTAL_RECORD_COUNT_PROP });
    }

    /**
     * {@inheritDoc}
     * 
     * @return component name
     */
    @Override
    public String getName() {
        return componentName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ExecutionEngine> getSupportedExecutionEngines() {
        return engines;
    }

    protected void assertEngineCompatibility(ExecutionEngine engine) throws TalendRuntimeException {
        if (!getSupportedExecutionEngines().contains(engine))
            TalendRuntimeException.build(ComponentsErrorCode.WRONG_EXECUTION_ENGINE) //
                    .put("component", getName()) //
                    .put("requested", engine == null ? "null" : engine.toString()) //
                    .put("available", getSupportedExecutionEngines().toString()).throwIt();
    }

    protected void assertConnectorTopologyCompatibility(ConnectorTopology connectorTopology) throws TalendRuntimeException {
        if (!getSupportedConnectorTopologies().contains(connectorTopology))
            TalendRuntimeException.build(ComponentsErrorCode.WRONG_CONNECTOR) //
                    .put("component", getName()) //
                    .put("requested", connectorTopology == null ? "null" : connectorTopology.toString()) //
                    .put("available", getSupportedExecutionEngines().toString()).throwIt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return componentName + "_icon32.png";
        }
        return null;
    }

    @Override
    public String[] getFamilies() {
        // Subclass me
        return new String[] {};
    }

    @Override
    protected String getI18nPrefix() {
        return "component."; //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public ComponentProperties createProperties() {
        ComponentProperties compProp = PropertiesImpl.createNewInstance(getPropertiesClass(), "root");
        compProp.init();
        return compProp;
    }

    @Override
    public ComponentProperties createRuntimeProperties() {
        ComponentProperties compProp = PropertiesImpl.createNewInstance(getPropertiesClass(), "root");
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

    @Override
    public boolean isRejectAfterClose() {
        return false;
    }

    @Override
    public boolean isStartable() {
        return getSupportedConnectorTopologies().contains(ConnectorTopology.OUTGOING);
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

    @Override
    public Class<ComponentProperties> getPropertiesClass() {
        return (Class<ComponentProperties>) getPropertyClass();
    }

    /**
     * @return the associated ComponentProperties class associated with the Component. This shall be used to initialised
     * the runtime classes.
     */
    // TODO remove this and use the getPropertiesClass()
    abstract public Class<? extends ComponentProperties> getPropertyClass();

    /**
     * return the list of ComponentProperties that may be assigned to nested properties of the main ComponentProperties
     * class(see {@link AbstractComponentDefinition#getPropertyClass()} associated with this definiton.<br/>
     * This method uses static class definition to avoid ComponentProperties instanciation.
     * 
     * @return return the list of ComponentProperties that may be assigned to a nested property of this component
     * associated ComponentProperties.
     */
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return (Class<? extends ComponentProperties>[]) Array.newInstance(Class.class, 0);
    }

    public Class<? extends ComponentProperties>[] concatPropertiesClasses(Class<? extends ComponentProperties>[] first,
            Class<? extends ComponentProperties>[] second) {
        Class<? extends ComponentProperties>[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * this will configure all i18n message formatter of the namedThings to the one associated with this instance.
     */
    public <T extends NamedThing> T[] setupI18N(T[] namedThings) {
        for (NamedThing nt : namedThings) {
            nt.setI18nMessageFormatter(getI18nMessageFormatter());
        }
        return namedThings;
    }

    @Override
    public String getImagePath() {
        return getPngImagePath(ComponentImageType.PALLETE_ICON_32X32);
    }

}
