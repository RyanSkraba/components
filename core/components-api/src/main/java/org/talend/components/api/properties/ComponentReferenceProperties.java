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
package org.talend.components.api.properties;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.DeserializeDeletedFieldHandler;

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
public class ComponentReferenceProperties<P extends Properties> extends ReferenceProperties<P>
        implements DeserializeDeletedFieldHandler {

    public enum ReferenceType {
        THIS_COMPONENT,
        COMPONENT_TYPE,
        COMPONENT_INSTANCE
    }

    //
    // Properties
    //
    public Property<ReferenceType> referenceType = newEnum("referenceType", ReferenceType.class);

    public Property<String> componentInstanceId = newString("componentInstanceId"); //$NON-NLS-1$

    public ComponentReferenceProperties(String name, String propDefinitionName) {
        super(name, propDefinitionName);
    }

    @Override
    public boolean deletedField(String fieldName, Object value) {
        boolean modified = false;
        if ("componentType".equals(fieldName)) {
            @SuppressWarnings("unchecked")
            Property<String> compTypeProp = (Property<String>) value;
            referenceDefinitionName.setValue(compTypeProp.getValue());
            modified = true;
        } else if ("componentProperties".equals(fieldName)) {
            Properties oldRef = (Properties) value;
            setReference(oldRef);
            modified = true;
        }

        return modified;
    }

}
