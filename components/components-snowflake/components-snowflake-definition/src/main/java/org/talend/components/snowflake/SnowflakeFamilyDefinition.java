// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseDefinition;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputDefinition;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputDefinition;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowDefinition;

import com.google.auto.service.AutoService;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the Snowflake family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + SnowflakeFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class SnowflakeFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Snowflake";

    public SnowflakeFamilyDefinition() {
        super(NAME,
                // Components
                new TSnowflakeConnectionDefinition(), new TSnowflakeRowDefinition(), new TSnowflakeInputDefinition(),
                new TSnowflakeOutputDefinition(), new TSnowflakeCloseDefinition(),
                // Component wizards
                new SnowflakeConnectionWizardDefinition(),
                // TODO remove the edit one
                new SnowflakeConnectionEditWizardDefinition()
        // TODO not sure it works, so comment it, keep like before
        // new SnowflakeTableWizardDefinition()
        );
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }

}
