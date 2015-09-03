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

import org.talend.components.api.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Layout;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("userPasswordProperties")
public class UserPasswordProperties extends ComponentProperties {

    public Property<String>    userId       = new Property<String>("userId", "User Id").setRequired(true);

    // FIXME - this needs to be encrypted, need to think about how to handle that
    public Property<String>    password     = new Property<String>("password", "Password").setRequired(true);

    public static final String USERPASSWORD = "UserPassword";

    public UserPasswordProperties() {
        Form form = new Form(this, USERPASSWORD, "User Password");
        form.addChild(userId, Layout.create().setRow(1));
        form.addChild(password, Layout.create().setRow(2));

    }

}
