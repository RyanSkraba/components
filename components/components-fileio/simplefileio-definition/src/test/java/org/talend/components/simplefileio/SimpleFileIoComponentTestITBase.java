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
package org.talend.components.simplefileio;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.simplefileio.input.SimpleFileIoInputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIoOutputDefinition;

public abstract class SimpleFileIoComponentTestITBase extends AbstractComponentTest {

    @Inject
    ComponentService componentService;

    @Override
    public ComponentService getComponentService() {
        return componentService;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertThat(getComponentService().getComponentDefinition(SimpleFileIoInputDefinition.NAME), notNullValue());
        assertThat(getComponentService().getComponentDefinition(SimpleFileIoOutputDefinition.NAME), notNullValue());
    }
}
