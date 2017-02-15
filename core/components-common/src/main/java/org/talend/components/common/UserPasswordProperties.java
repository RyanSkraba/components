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
package org.talend.components.common;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.EnumSet;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class UserPasswordProperties extends PropertiesImpl {

    public Property<Boolean> useAuth = newBoolean("useAuth", false);

    public Property<String> userId = newProperty("userId");

    public Property<String> password = newProperty("password")
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    private boolean needSwitch = false;

    public UserPasswordProperties(String name) {
        super(name);
    }

    public UserPasswordProperties(String name, Boolean withSwitch) {
        super(name);
        needSwitch = withSwitch;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        if (needSwitch) {
            useAuth.setRequired();
        } else {
            userId.setRequired();
            password.setRequired();
        }
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        if (needSwitch) {
            form.addRow(useAuth);
        }
        form.addRow(userId);
        form.addRow(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

    public void afterUseAuth() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            if (needSwitch) {
                form.getWidget(userId).setVisible(useAuth);
                form.getWidget(password).setVisible(useAuth);
            }
        }
    }

}
