package org.talend.components.bigquery;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.bigquery.input.BigQueryInputDefinition;
import org.talend.components.bigquery.output.BigQueryOutputDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public abstract class BigQueryComponentTestITBase extends AbstractComponentTest2 {

    @Inject
    DefinitionRegistryService defReg;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return defReg;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(BigQueryInputDefinition.NAME),
                notNullValue());
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(BigQueryOutputDefinition.NAME),
                notNullValue());
    }
}
