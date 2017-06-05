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
package org.talend.components.service.rest.fullexample;

import aQute.bnd.annotation.component.Component;
import com.google.auto.service.AutoService;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.service.rest.fullexample.dataset.FullExampleDatasetDefinition;
import org.talend.components.service.rest.fullexample.datastore.FullExampleDatastoreDefinition;

/**
 * Install all of the definitions provided for the FullExample family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + FullExampleFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class FullExampleFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "FullExample";

    public FullExampleFamilyDefinition() {
        super(NAME, new FullExampleDatastoreDefinition(), new FullExampleDatasetDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
