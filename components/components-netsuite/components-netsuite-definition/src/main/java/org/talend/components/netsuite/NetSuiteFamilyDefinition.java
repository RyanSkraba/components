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

package org.talend.components.netsuite;

import aQute.bnd.annotation.component.Component;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.netsuite.connection.NetSuiteConnectionDefinition;
import org.talend.components.netsuite.input.NetSuiteInputDefinition;
import org.talend.components.netsuite.output.NetSuiteOutputDefinition;

/**
 * Definition of NetSuite component family.
 */
@Component(
        name = Constants.COMPONENT_INSTALLER_PREFIX + NetSuiteFamilyDefinition.NAME,
        provide = ComponentInstaller.class
)
public class NetSuiteFamilyDefinition extends AbstractComponentFamilyDefinition
        implements ComponentInstaller {

    public static final String NAME = "NetSuite";

    public NetSuiteFamilyDefinition() {
        super(NAME,
                new NetSuiteConnectionDefinition(),
                new NetSuiteInputDefinition(),
                new NetSuiteOutputDefinition()
        );
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }

}
