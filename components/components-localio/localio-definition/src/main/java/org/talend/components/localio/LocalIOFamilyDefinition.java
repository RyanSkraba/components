// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.localio;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.localio.fixedflowinput.FixedFlowInputDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the processing family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + LocalIOFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class LocalIOFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "LocalIO";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_ARTIFACT_ID = "localio-runtime";

    public LocalIOFamilyDefinition() {
        super(NAME,
                // Components
                new FixedFlowInputDefinition()

                // Component wizards
        );
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
