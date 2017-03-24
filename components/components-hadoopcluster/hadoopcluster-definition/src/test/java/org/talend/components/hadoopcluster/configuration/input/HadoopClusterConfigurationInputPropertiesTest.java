package org.talend.components.hadoopcluster.configuration.input;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class HadoopClusterConfigurationInputPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    HadoopClusterConfigurationInputProperties properties;

    @Before
    public void reset() {
        properties = new HadoopClusterConfigurationInputProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    @Test
    public void testVisible() throws Throwable {
        Form main = properties.getForm(Form.MAIN);
        assertTrue(main.getWidget(properties.clusterManagerType).isVisible());
        assertTrue(main.getWidget(properties.url).isVisible());
        assertTrue(main.getWidget(properties.basicAuth).isVisible());
        assertTrue(main.getWidget(properties.ssl).isVisible());
        assertTrue(main.getWidget(properties.schema).isVisible());
        assertTrue(main.getWidget(properties.blackList).isVisible());
    }

    @Test
    public void testDefaultProperties() {
        assertEquals(HadoopClusterConfigurationInputProperties.ClusterManagerType.CLOUDERA_MANAGER,
                properties.clusterManagerType.getValue());
        assertEquals(HadoopClusterConfiguration.schema, properties.schema.schema.getValue());
    }

    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();

        List<String> ALL = Arrays.asList(properties.clusterManagerType.getName(), properties.url.getName(),
                properties.basicAuth.getName(), properties.ssl.getName(), properties.schema.getName(),
                properties.blackList.getName());

        Assert.assertThat(main, notNullValue());
        Assert.assertThat(mainWidgets, hasSize(ALL.size()));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }
        ;
    }
}
