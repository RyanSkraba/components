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
package org.talend.components.salesforce.tsalesforceoutput;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

public class TSalesforceOutputProperties extends ComponentProperties {

    public enum OutputAction {
        INSERT,
        UPDATE,
        UPSERT,
        DELETE
    }

    public Property<OutputAction>         outputAction    = new Property<>("outputAction", "Output Action");

    public Property<String>               upsertKeyColumn = new Property<String>("upsertKeyColumn", "Upsert Key Column");

    public SalesforceConnectionProperties connection      = new SalesforceConnectionProperties();

    public SalesforceModuleProperties     module          = new SalesforceModuleProperties(connection);

    public TSalesforceOutputProperties() {
        setupLayout();
    }

    public static final String MAIN = "Main";

    @Override
    public void setupLayout() {
        Form connectionForm = Form.create(this, MAIN, "Salesforce Output");

    }
}
