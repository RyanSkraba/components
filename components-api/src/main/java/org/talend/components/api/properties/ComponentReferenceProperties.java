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
package org.talend.components.api.properties;

import static org.talend.daikon.properties.Property.Flags.*;
import static org.talend.daikon.properties.PropertyFactory.*;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;

import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

/**
 * A reference to another component. This could be in one of the following states:
 * <li>Use this component (no reference)</li>
 * <li>Reference a single instance of a given component type in the enclosing scope, e.g. Job</li>
 * <li>Reference to a particular instance of a component. In this case, the {@link #componentProperties} will be
 * populated by the {@link org.talend.daikon.properties.presentation.Widget}.</li>
 *
 * IMPORTANT - when using {@code ComponentReferenceProperties} the property name in the enclosingProperties must be
 * {@code referencedComponent}.
 *
 * The {@link org.talend.daikon.properties.presentation.WidgetType#COMPONENT_REFERENCE} uses this class as its
 * properties and the Widget will populate these values.
 */
public class ComponentReferenceProperties extends ComponentProperties {

    public enum ReferenceType {
        THIS_COMPONENT,
        COMPONENT_TYPE,
        COMPONENT_INSTANCE
    }

    //
    // Properties
    //
    public Property<ReferenceType> referenceType = newEnum("referenceType", ReferenceType.class);

    public Property<String> componentType = newProperty("componentType").setFlags(EnumSet.of(DESIGN_TIME_ONLY)); //$NON-NLS-1$

    public Property<String> componentInstanceId = newProperty("componentInstanceId"); //$NON-NLS-1$

    /**
     * The properties associated with the referenced component. This can be used at design time. This is non-null only
     * if there is a componentInstanceId specified.
     */
    public ComponentProperties componentProperties;

    /**
     * The properties that encloses this object. The field name of this object in the enclosing properties must be
     * {@code referencedComponent}.
     */
    public ComponentReferencePropertiesEnclosing enclosingProperties;

    public ComponentReferenceProperties(String name, ComponentReferencePropertiesEnclosing enclosing) {
        super(name);
        this.enclosingProperties = enclosing;
    }

    // IMPORTANT - this is the name of the property in the enclosingProperties that uses this
    // ComponentReferenceProperties
    public void afterReferencedComponent() {
        if (enclosingProperties != null) {
            enclosingProperties.afterReferencedComponent();
        }
    }

    @Override
    public List<Form> getForms() {
        if (enclosingProperties != null) {
            return ((ComponentProperties) enclosingProperties).getForms();
        }
        return super.getForms();
    }

    @Override
    protected boolean acceptUninitializedField(Field f) {
        if (super.acceptUninitializedField(f)) {
            return true;
        }
        // we accept that return field is not intialized after setupProperties.
        return "componentProperties".equals(f.getName());
    }

}
