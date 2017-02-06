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

package org.talend.components.simplefileio;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.simplefileio.input.SimpleFileIOInputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIOOutputDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the SimpleFileIO family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + SimpleFileIOComponentFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class SimpleFileIOComponentFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    // The best practice is to use SimpleFileIO to align with Beam, but we don't want to change this identifier.
    public static final String NAME = "SimpleFileIo";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID = "simplefileio-runtime";

    public static final String MAVEN_DEFAULT_RUNTIME_URI = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID;

    public SimpleFileIOComponentFamilyDefinition() {
        super(NAME, new SimpleFileIODatastoreDefinition(), new SimpleFileIODatasetDefinition(),
                new SimpleFileIOInputDefinition(), new SimpleFileIOOutputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
