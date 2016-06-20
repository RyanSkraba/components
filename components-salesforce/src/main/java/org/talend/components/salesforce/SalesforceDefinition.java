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

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

public abstract class SalesforceDefinition extends AbstractComponentDefinition {
    
    public SalesforceDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Business/Salesforce", "Cloud/Salesforce" };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { SalesforceConnectionProperties.class };
    }

    @Override
    // Most of the components are on the input side, so put this here, the output definition will override this
    public Property[] getReturnProperties() {
        return new Property[] { newInteger(RETURN_TOTAL_RECORD_COUNT) };
    }

    @Override
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-salesforce";
    }

}
