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

public class ProxyProperties extends ComponentProperties {

    public SchemaElement useProxy = newProperty(SchemaElement.Type.BOOLEAN, "useProxy").setRequired(true); //$NON-NLS-1$

    public SchemaElement host = newProperty("host").setRequired(true); //$NON-NLS-1$

    public UserPasswordProperties userPassword = new UserPasswordProperties(); //$NON-NLS-1$

    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "Proxy Parameters");
        form.addRow(useProxy);
        form.addRow(host);
        form.addRow(userPassword.getForm(Form.MAIN));
    }

}
