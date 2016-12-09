package ${package};

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import ${packageTalend}.api.service.ComponentService;
import ${packageTalend}.api.test.AbstractComponentTest;
import ${packageTalend}.${componentNameLowerCase}.input.${componentNameClass}InputDefinition;
import ${packageTalend}.${componentNameLowerCase}.output.${componentNameClass}OutputDefinition;

public abstract class ${componentNameClass}ComponentTestITBase extends AbstractComponentTest {

    @Inject
    ComponentService componentService;

    @Override
    public ComponentService getComponentService() {
        return componentService;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertThat(getComponentService().getComponentDefinition(${componentNameClass}InputDefinition.NAME), notNullValue());
        assertThat(getComponentService().getComponentDefinition(${componentNameClass}OutputDefinition.NAME), notNullValue());
    }
}
