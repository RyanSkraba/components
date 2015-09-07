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

import com.fasterxml.jackson.annotation.JsonRootName;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Layout;
import org.talend.components.api.properties.presentation.Wizard;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.oauth.OauthProperties;

import java.util.List;

@JsonRootName("salesforceModuleProperties") public class SalesforceModuleProperties extends ComponentProperties {

    private SalesforceConnectionProperties connection;

    //
    // Properties
    //
    public Property<String> moduleName = new Property<String>("moduleName", "Module Name");

    public static final String MODULE = "Module";

    public static final String ADVANCED = "Advanced";

    // FIXME - OK what about if we are using a connection from a separate component
    // that defines the connection, how do we get that separate component?
    public SalesforceModuleProperties(SalesforceConnectionProperties connectionProperties) {
        super();
        connection = connectionProperties;
        setupLayout();
    }

    @Override protected void setupLayout() {
        super.setupLayout();

        Form moduleForm = Form.create(this, MODULE, "Salesforce Module");
        moduleForm.addChild(moduleName, Layout.create().setRow(1).setWidgetType(Layout.WidgetType.LISTBOX));
    }

    public void beforeModuleName() throws Exception {
        SalesforceRuntime conn = new SalesforceRuntime();
        conn.connect(connection);
        List<String> moduleNames = conn.getModuleNames();
        moduleName.setPossibleValues(moduleNames);
    }

}
