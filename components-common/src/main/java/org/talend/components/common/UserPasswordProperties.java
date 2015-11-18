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

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class UserPasswordProperties extends ComponentProperties {

    public SchemaElement userId = newProperty("userId").setRequired(true); //$NON-NLS-1$

    // FIXME - this needs to be encrypted, need to think about how to handle that
    public SchemaElement password = newProperty("password").setRequired(true); //$NON-NLS-1$

    @Override
    protected void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "User Password");
        form.addRow(userId);
        form.addRow(password);
    }

}
