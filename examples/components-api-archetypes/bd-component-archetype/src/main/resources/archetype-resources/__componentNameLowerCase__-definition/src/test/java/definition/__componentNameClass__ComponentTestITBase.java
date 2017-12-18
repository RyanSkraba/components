package ${package}.definition;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import ${packageTalend}.api.test.AbstractComponentTest2;
import ${packageTalend}.${componentNameLowerCase}.definition.input.${componentNameClass}InputDefinition;
import ${packageTalend}.${componentNameLowerCase}.definition.output.${componentNameClass}OutputDefinition;
import ${packageDaikon}.definition.Definition;
import ${packageDaikon}.definition.service.DefinitionRegistryService;


public abstract class ${componentNameClass}ComponentTestITBase extends AbstractComponentTest2 {

    @Inject
    DefinitionRegistryService defReg;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return defReg;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(${componentNameClass}InputDefinition.NAME), notNullValue());
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(${componentNameClass}OutputDefinition.NAME), notNullValue());
    }
}
