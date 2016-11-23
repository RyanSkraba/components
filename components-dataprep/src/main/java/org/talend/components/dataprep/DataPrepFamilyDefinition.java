// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputDefinition;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the DataPrep family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + DataPrepFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class DataPrepFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "DataPrep";

    public DataPrepFamilyDefinition() {
        super(NAME,
                // Components
                new TDataSetInputDefinition(), new TDataSetOutputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
