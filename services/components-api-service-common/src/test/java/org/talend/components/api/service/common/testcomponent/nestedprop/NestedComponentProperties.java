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
package org.talend.components.api.service.common.testcomponent.nestedprop;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.presentation.Form;

public class NestedComponentProperties extends ComponentPropertiesImpl {

    public static final String A_GREAT_PROP_NAME = "aGreatProperty"; //$NON-NLS-1$

    public Property<String> aGreatProperty = newProperty(A_GREAT_PROP_NAME);

    public Property<String> anotherProp = newProperty("anotherProp");

    public NestedComponentProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(aGreatProperty);
        form.addRow(anotherProp);
    }

}