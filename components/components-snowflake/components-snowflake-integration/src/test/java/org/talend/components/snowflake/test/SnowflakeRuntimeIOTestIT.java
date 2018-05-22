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
package org.talend.components.snowflake.test;

import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.snowflake.SnowflakeFamilyDefinition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class SnowflakeRuntimeIOTestIT extends SnowflakeRuntimeIT {

    @Override
    protected DefinitionRegistryService getDefinitionService() {
        DefinitionRegistry definitionRegistry = new DefinitionRegistry();
        definitionRegistry.registerComponentFamilyDefinition(new SnowflakeFamilyDefinition());
        return definitionRegistry;
    }

}
