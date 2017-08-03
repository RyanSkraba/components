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
package org.talend.components.common.format;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.common.format.instances.AbstractTestFormatDefinition;
import org.talend.components.common.format.instances.AbstractTestFormatProperties;
import org.talend.components.common.format.instances.TestFormatDefinition1Impl;
import org.talend.components.common.format.instances.TestFormatDefinition2Impl;
import org.talend.components.common.format.instances.TestFormatProperties2Impl;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;

public class FormatPropertiesProviderTest {

    @Test
    public void testProperties() throws Throwable {
        FormatPropertiesProvider<AbstractTestFormatProperties> propertiesProvider = new FormatPropertiesProvider<AbstractTestFormatProperties>(
                "propertiesProvider", AbstractTestFormatDefinition.class) {
        };
        DefinitionRegistry reg = new DefinitionRegistry();
        TestFormatDefinition1Impl def1 = new TestFormatDefinition1Impl();
        TestFormatDefinition2Impl def2 = new TestFormatDefinition2Impl();
        reg.registerDefinition(Arrays.asList(def1, def2));
        reg.injectDefinitionRegistry(propertiesProvider);
        propertiesProvider.init();

        // Check that possible format values include both of registered definitions
        Assert.assertEquals(propertiesProvider.format.getPossibleValues().size(), 2);

        // Set the format value to first registered definition
        propertiesProvider.format.setValue(def1.getName());

        PropertiesDynamicMethodHelper.afterProperty(propertiesProvider, propertiesProvider.format.getName());

        AbstractTestFormatProperties formatProperties = propertiesProvider.getFormatProperties();

        Assert.assertNotEquals(def2.getName(), propertiesProvider.format.getValue());
        Assert.assertEquals(def1.getName(), propertiesProvider.format.getValue());
        // Format properties class in PropertiesProvider should be the same as properties class for definition
        Assert.assertEquals(def1.getPropertiesClass(), formatProperties.getClass());

        propertiesProvider.format.setValue(def2.getName());

        PropertiesDynamicMethodHelper.afterProperty(propertiesProvider, propertiesProvider.format.getName());

        formatProperties = propertiesProvider.getFormatProperties();

        Assert.assertNotEquals(def1.getName(), propertiesProvider.format.getValue());
        Assert.assertEquals(def2.getName(), propertiesProvider.format.getValue());
        Assert.assertEquals(TestFormatProperties2Impl.class, formatProperties.getClass());
    }

}
