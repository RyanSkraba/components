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
package org.talend.components.api.test.testcomponent;

import static org.talend.components.api.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;

public class TestComponentProperties extends ComponentProperties {

    public Property<String>    userId        = new Property<String>("userId", "User Id").setRequired(true);

    public Property<String>    password      = new Property<String>("password", "Password").setRequired(true);

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties() {
        Form form = Form.create(this, TESTCOMPONENT, "Test Component");
        form.addChild(widget(userId).setRow(1));
        form.addChild(widget(password).setRow(2));

    }
}
