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

import org.osgi.service.component.annotations.Component;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.localio.devnull.DevNullOutputDefinition;
import org.talend.components.localio.fixed.FixedDatasetDefinition;
import org.talend.components.localio.fixed.FixedDatastoreDefinition;
import org.talend.components.localio.fixed.FixedInputDefinition;
import org.talend.components.localio.fixedflowinput.FixedFlowInputDefinition;

import com.google.auto.service.AutoService;

/**
 * Install all of the definitions provided for the LocalIO family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX
        + LocalIOComponentFamilyDefinition.NAME, service = ComponentInstaller.class)
public class LocalIOComponentFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "LocalIO";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID = "localio-runtime";

    public static final String MAVEN_DEFAULT_RUNTIME_URI = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID;

    public LocalIOComponentFamilyDefinition() {
        super(NAME,
                new FixedDatastoreDefinition(), new FixedDatasetDefinition(), new FixedInputDefinition(), new DevNullOutputDefinition(),
                new FixedFlowInputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
