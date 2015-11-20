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
package org.talend.components.api.service.testcomponent;

import static org.talend.components.api.schema.SchemaFactory.newProperty;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;

public class TestComponentProperties extends ComponentProperties {

    public static final String USER_ID_PROP_NAME = "userId"; //$NON-NLS-1$

    public SchemaElement userId = newProperty(USER_ID_PROP_NAME).setRequired(true);

    public SchemaElement password = newProperty("password").setRequired(true);

    public NestedComponentProperties nestedProps = new NestedComponentProperties("nestedProps");

    public ComponentPropertiesWithDefinedI18N nestedProp2 = new ComponentPropertiesWithDefinedI18N("nestedProp2");

    public InheritedComponentProperties nestedProp3 = new InheritedComponentProperties("nestedProp3");

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties(String name) {
        super(name);
        init();
    }

    public ComponentProperties init() {
        super.init();
        Form form = Form.create(this, TESTCOMPONENT, "Test Component");
        form.addRow(userId);
        form.addRow(password);
        return this;
    }
}
