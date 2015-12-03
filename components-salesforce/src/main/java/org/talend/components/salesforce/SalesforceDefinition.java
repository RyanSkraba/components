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

import java.io.InputStream;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.runtime.ComponentRuntime;

public abstract class SalesforceDefinition extends AbstractComponentDefinition {

    protected String componentName;

    public SalesforceDefinition(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Business/Salesforce", "Cloud/Salesforce" };
    }

    @Override
    public ComponentRuntime createRuntime() {
        return new SalesforceRuntime();
    }

    @Override
    public String getName() {
        return componentName;
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return componentName.replace("New", "") + "_icon32.png"; //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        // FIXME
        return getName();
    }

    @Override
    public InputStream getMavenPom() {
        return this.getClass().getResourceAsStream("/org/talend/components/salesforce/pom.xml");
    }

}
