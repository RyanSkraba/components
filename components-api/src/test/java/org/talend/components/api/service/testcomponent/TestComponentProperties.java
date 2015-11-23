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

import static org.talend.components.api.properties.PropertyFactory.newProperty;
import static org.talend.components.api.properties.presentation.Widget.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;

public class TestComponentProperties extends ComponentProperties {

    public static final String USER_ID_PROP_NAME = "userId"; //$NON-NLS-1$

    public Property userId = (Property) newProperty(USER_ID_PROP_NAME).setRequired(true);

    public Property password = (Property) newProperty("password").setRequired(true);

    public Property nameList = newProperty("nameList");

    public Property nameListRef = newProperty("nameListRef");

    public NestedComponentProperties nestedProps = new NestedComponentProperties("nestedProps");

    public ComponentPropertiesWithDefinedI18N nestedProp2 = new ComponentPropertiesWithDefinedI18N("nestedProp2");

    public InheritedComponentProperties nestedProp3 = new InheritedComponentProperties("nestedProp3");

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties(String name) {
        super(name);
        init();
    }

    public void beforeNameList() {
        List values = new ArrayList<>();
        Collections.addAll(values, new String[] { "name1", "name2", "name3" });
        nameList.setPossibleValues(values);
    }

    public void beforeNameListRef() {
        List values = new ArrayList<>();
        Collections.addAll(values, new String[] { "namer1", "namer2", "namer3" });
        nameListRef.setPossibleValues(values);
    }

    public ComponentProperties init() {
        super.init();
        Form form = Form.create(this, Form.MAIN, "Test Component");
        form.addRow(userId);
        form.addRow(password);
        form.addRow(widget(nameList).setWidgetType(Widget.WidgetType.NAME_SELECTION_AREA));
        form.addRow(widget(nameListRef).setWidgetType(Widget.WidgetType.NAME_SELECTION_REFERENCE));
        return this;
    }
}
