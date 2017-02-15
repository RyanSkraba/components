package org.talend.components.elasticsearch;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.elasticsearch.input.ElasticsearchInputDefinition;
import org.talend.components.elasticsearch.output.ElasticsearchOutputDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public abstract class ElasticsearchComponentTestITBase extends AbstractComponentTest2 {

    @Inject
    DefinitionRegistryService defReg;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return defReg;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(ElasticsearchInputDefinition.NAME),
                notNullValue());
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(ElasticsearchOutputDefinition.NAME),
                notNullValue());
    }
}
