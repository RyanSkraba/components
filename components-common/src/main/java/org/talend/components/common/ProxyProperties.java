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

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class ProxyProperties extends ComponentProperties {

    public SchemaElement useProxy = newProperty(SchemaElement.Type.BOOLEAN, "useProxy").setRequired(true); //$NON-NLS-1$

    public SchemaElement host = newProperty("host").setRequired(true); //$NON-NLS-1$

    public UserPasswordProperties userPassword;// see constructor

    public static final String PROXY = "Proxy"; //$NON-NLS-1$

    public ProxyProperties(I18nMessageProvider i18nMessagesProvider) {
        super(i18nMessagesProvider, "org.talend.components.common.messages"); //$NON-NLS-1$
        userPassword = new UserPasswordProperties(i18nMessagesProvider);
        Form form = Form.create(this, PROXY, "Proxy Parameters"); //$NON-NLS-1$
        form.addRow(useProxy);
        form.addRow(host);
        form.addRow(userPassword.getForm(UserPasswordProperties.USERPASSWORD));
    }

}
