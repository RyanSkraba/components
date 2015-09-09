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

import com.fasterxml.jackson.annotation.JsonRootName;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;

/**
 * A reference to another component. This could be in one of the following states:
 * <li>Use this component (no reference)</li>
 * <li>Reference a single instance of a given component type in the enclosing scope, e.g. Job</li>
 * <li>Reference to a particular instance of a component</li>
 */
@JsonRootName("schema") public class ComponentReferenceProperties extends ComponentProperties {

    public enum ReferenceType {THIS_COMPONENT, COMPONENT_TYPE, COMPONENT_INSTANCE}

    //
    // Properties
    //
    public Property<ReferenceType> referenceType = new Property<ReferenceType>("referenceType", "Reference Type");

    public Property<String> componentType = new Property<String>("componentType", "Component Type");

    public Property<String> componentInstanceId = new Property<String>("componentInstanceId", "Component Instance");

    public static final String REFERENCE = "Reference";

    public ComponentReferenceProperties() {
        super();
        setupLayout();
    }

    @Override protected void setupLayout() {
        super.setupLayout();

        Form reference = Form.create(this, REFERENCE, "Component");
        // FIXME - handle this with the Layout refactor
        //        reference.addChild(this.schema, Layout.create().setRow(2).setWidgetType(Layout.WidgetType.COMPONENT_REFERENCE));
        refreshLayout(reference);
    }

}
