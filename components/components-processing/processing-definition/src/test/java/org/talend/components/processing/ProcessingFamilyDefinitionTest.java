package org.talend.components.processing;

import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;

import org.hamcrest.Matcher;
import org.talend.components.processing.ProcessingFamilyDefinition;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class ProcessingFamilyDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ProcessingFamilyDefinition def = new ProcessingFamilyDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    /**
     * The component family for this component is also the {@link ComponentInstaller}.
     */
    @Test
    public void testInstall() {
        ((ComponentInstaller) def).install(ctx);
        Mockito.verify(ctx, times(1)).registerComponentFamilyDefinition(any(ComponentFamilyDefinition.class));
    }

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("Processing"));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("Window"))));
    }

}
