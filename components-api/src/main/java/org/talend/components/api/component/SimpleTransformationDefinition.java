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
package org.talend.components.api.component;

import java.io.InputStream;

import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ProcessorComponentDefinition;

/**
 * Simple transformation component. This component support one input and one output.
 *
 * It contains the main schema, defined by the user.
 *
 */
public abstract class SimpleTransformationDefinition extends ProcessorComponentDefinition {

    private String componentName;

    private String pomPath;

    public SimpleTransformationDefinition(String componentName, String pomPath) {
        this.componentName = componentName;
        this.pomPath = pomPath;
        setConnectors(new ComponentConnector(ComponentConnector.Type.FLOW, 1, 1));
        // No Trigger
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
        return this.getClass().getResourceAsStream(pomPath);
    }

}
