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
