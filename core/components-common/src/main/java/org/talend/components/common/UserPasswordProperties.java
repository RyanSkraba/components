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

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.EnumSet;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class UserPasswordProperties extends PropertiesImpl {

    public Property<String> userId = newProperty("userId").setRequired(); //$NON-NLS-1$

    public Property<String> password = newProperty("password").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public UserPasswordProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(userId);
        form.addRow(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

}
