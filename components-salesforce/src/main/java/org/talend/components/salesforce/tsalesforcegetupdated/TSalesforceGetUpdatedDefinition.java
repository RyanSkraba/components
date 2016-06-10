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
package org.talend.components.salesforce.tsalesforcegetupdated;

import aQute.bnd.annotation.component.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.runtime.SalesforceSource;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceGetUpdatedDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceGetUpdatedDefinition extends SalesforceDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "tSalesforceGetUpdated"; //$NON-NLS-1$

    public TSalesforceGetUpdatedDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String getPartitioning() {
        return AUTO;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSalesforceGetUpdatedProperties.class;
    }

    @Override
    public Source getRuntime() {
        return new SalesforceSource();
    }

}
