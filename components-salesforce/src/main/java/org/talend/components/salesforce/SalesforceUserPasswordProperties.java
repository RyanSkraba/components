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
package org.talend.components.salesforce;

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.common.UserPasswordProperties;

public class SalesforceUserPasswordProperties extends UserPasswordProperties {

    public SchemaElement securityKey = newString("securityKey").setRequired(); //$NON-NLS-1$

    @Override
    protected void setupLayout() {
        super.setupLayout();
        Form form = getForm(Form.MAIN);
        form.addColumn(securityKey);
    }

}
