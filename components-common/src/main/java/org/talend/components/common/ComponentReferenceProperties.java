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
package org.talend.components.common;

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * A reference to another component. This could be in one of the following states:
 * <li>Use this component (no reference)</li>
 * <li>Reference a single instance of a given component type in the enclosing scope, e.g. Job</li>
 * <li>Reference to a particular instance of a component</li>
 */
public class ComponentReferenceProperties extends ComponentProperties {

    public enum ReferenceType {
        THIS_COMPONENT,
        COMPONENT_TYPE,
        COMPONENT_INSTANCE
    }

    public static class InnerProperties extends ComponentProperties {

        public InnerProperties(String name) {
            super(name);
        }

        //
        // Properties
        //
        public Property referenceType = (Property) newProperty("referenceType").setEnumClass(ReferenceType.class); //$NON-NLS-1$

        public Property componentType = newProperty("componentType"); //$NON-NLS-1$

        public Property componentInstanceId = newProperty("componentInstanceId"); //$NON-NLS-1$

    }

    public InnerProperties properties = new InnerProperties("properties");

    public ComponentReferenceProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form reference = Form.create(this, Form.REFERENCE, "Reference");
        reference.addRow(widget(properties).setWidgetType(Widget.WidgetType.COMPONENT_REFERENCE));
    }

}
