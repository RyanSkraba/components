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

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class TestComponentProperties extends ComponentProperties {

    public static final String USER_ID_PROP_NAME = "userId"; //$NON-NLS-1$

    public SchemaElement userId = newProperty(USER_ID_PROP_NAME).setRequired(true);

    public SchemaElement password = newProperty("password").setRequired(true);

    public NestedComponentProperties nestedProps;

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties() {
        Form form = Form.create(this, TESTCOMPONENT, "Test Component");
        form.addRow(userId);
        form.addRow(password);
        nestedProps = new NestedComponentProperties("thenestedproperty");
    }
}
