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

import static org.talend.components.api.schema.SchemaFactory.newSchemaElement;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class ProxyProperties extends ComponentProperties {

    public SchemaElement useProxy = newSchemaElement(SchemaElement.Type.BOOLEAN, "useProxy", "Use Proxy").setRequired(true);

    public SchemaElement host = newSchemaElement("host", "Host").setRequired(true);

    public UserPasswordProperties userPassword = new UserPasswordProperties();

    public static final String PROXY = "Proxy";

    public ProxyProperties() {
        Form form = Form.create(this, PROXY, "Proxy Parameters");
        form.addRow(useProxy);
        form.addRow(host);
        form.addRow(userPassword.getForm(UserPasswordProperties.USERPASSWORD));
    }

}
