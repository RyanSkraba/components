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
package org.talend.components.marklogic;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.marklogic.data.MarkLogicDatasetDefinition;
import org.talend.components.marklogic.data.MarkLogicDatastoreDefinition;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadDefinition;
import org.talend.components.marklogic.tmarklogicclose.MarkLogicCloseDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputDefinition;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputDefinition;
import org.talend.components.marklogic.wizard.MarkLogicWizardDefinition;

import com.google.auto.service.AutoService;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the FileInput family of components.
 */
@AutoService(ComponentInstaller.class) @Component(name = Constants.COMPONENT_INSTALLER_PREFIX
        + MarkLogicFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class MarkLogicFamilyDefinition
        extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "MarkLogic";

    public MarkLogicFamilyDefinition() {
        super(NAME,
                new MarkLogicInputDefinition(), new MarkLogicOutputDefinition(), new MarkLogicConnectionDefinition(),
                new MarkLogicCloseDefinition(), new MarkLogicBulkLoadDefinition(), new MarkLogicWizardDefinition(),
                // Datastore & Dataset definitions.
                new MarkLogicDatasetDefinition(), new MarkLogicDatastoreDefinition());

    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
