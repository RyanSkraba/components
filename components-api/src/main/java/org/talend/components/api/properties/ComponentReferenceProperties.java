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

import static org.talend.daikon.properties.PropertyFactory.newProperty;

import java.lang.reflect.Field;

import org.talend.daikon.properties.Property;

/**
 * A reference to another component. This could be in one of the following states:
 * <li>Use this component (no reference)</li>
 * <li>Reference a single instance of a given component type in the enclosing scope, e.g. Job</li>
 * <li>Reference to a particular instance of a component. In this case, the {@link #componentProperties} will be
 * populated by the {@link org.talend.daikon.properties.presentation.Widget}.</li>
 *
 * The {@link org.talend.daikon.properties.presentation.Widget.WidgetType#COMPONENT_REFERENCE} uses this class as its
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
    public Property referenceType = (Property) newProperty("referenceType").setEnumClass(ReferenceType.class); //$NON-NLS-1$

    public Property componentType = newProperty("componentType"); //$NON-NLS-1$

    public Property componentInstanceId = newProperty("componentInstanceId"); //$NON-NLS-1$

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

    public void afterReferencedComponent() {
        if (enclosingProperties != null)
            enclosingProperties.afterReferencedComponent();
    }

    @Override
    protected boolean acceptUninitializedField(Field f) {
        if (super.acceptUninitializedField(f))
            return true;
        // we accept that return field is not intialized after setupProperties.
        return "componentProperties".equals(f.getName());
    }

}
