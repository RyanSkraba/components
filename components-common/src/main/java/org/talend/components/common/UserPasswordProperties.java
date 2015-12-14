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

import static org.talend.components.api.properties.PropertyFactory.*;

import java.util.EnumSet;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;

public class UserPasswordProperties extends ComponentProperties {

    public Property userId = (Property) newProperty("userId").setRequired(true); //$NON-NLS-1$

    public Property password = ((Property) newProperty("password").setRequired(true))
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING, Property.Flags.UI_PASSWORD));

    public UserPasswordProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "User Password");
        form.addRow(userId);
        form.addRow(password);
    }

}
