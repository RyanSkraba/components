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
import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;

public class TestComponentProperties extends ComponentProperties {

    public static final String USER_ID_PROP_NAME = "userId"; //$NON-NLS-1$

    public static Form mainForm;
    public static Form restoreForm;

    public PresentationItem testPI = new PresentationItem("testPI", "testPI display name");

    public Property userId = (Property) newProperty(USER_ID_PROP_NAME).setRequired(true);

    public Property password = (Property) newProperty("password").setRequired(true);

    public Property nameList = newProperty("nameList");

    public Property nameListRef = newProperty("nameListRef");

    public Property integer = newProperty(Type.INT, "integer");

    public Property decimal = newProperty(Type.INT, "decimal");

    public Property date = newProperty(Type.DATE, "date");

    public Property dateTime = newProperty(Type.DATETIME, "dateTime");

    public NestedComponentProperties nestedProps = (NestedComponentProperties) new NestedComponentProperties("nestedProps");

    public ComponentPropertiesWithDefinedI18N nestedProp2 = (ComponentPropertiesWithDefinedI18N) new ComponentPropertiesWithDefinedI18N(
            "nestedProp2");

    public InheritedComponentProperties nestedProp3 = (InheritedComponentProperties) new InheritedComponentProperties(
            "nestedProp3");

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties(String name) {
        super(name);
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
        mainForm = form;
        form.addRow(userId);
        form.addRow(password);
        form.addRow(testPI);
        form.addRow(widget(nameList).setWidgetType(Widget.WidgetType.NAME_SELECTION_AREA));
        form.addRow(widget(nameListRef).setWidgetType(Widget.WidgetType.NAME_SELECTION_REFERENCE));

        form = Form.create(this, "restoreTest", "Restore Test");
        restoreForm = form;
        form.addRow(userId);
        form.addRow(nameList);
        form.addRow(integer);
        form.addRow(decimal);
        form.addRow(date);
        form.addRow(dateTime);
        form.addRow(nestedProps.getForm(Form.MAIN));
        return this;

    }
}
