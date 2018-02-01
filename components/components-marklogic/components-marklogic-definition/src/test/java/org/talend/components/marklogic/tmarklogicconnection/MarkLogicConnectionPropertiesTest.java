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
package org.talend.components.marklogic.tmarklogicconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.sandbox.SandboxInstanceFactory;
import org.talend.daikon.sandbox.SandboxedInstance;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SandboxInstanceFactory.class)
public class MarkLogicConnectionPropertiesTest {

    public static final String EXPECTED_DEFAULT_HOST = "127.0.0.1";
    public static final Integer EXPECTED_DEFAULT_PORT = 8000;
    public static final String EXPECTED_DEFAULT_DATABASE = "Documents";

    MarkLogicConnectionProperties testConnectionProperties;

    @Before
    public void setUp() {
        testConnectionProperties = new MarkLogicConnectionProperties("testConnectionProperties");
    }

    @Test
    public void testSetupProperties() {
        String expectedDefaultAuthentication = "DIGEST";

        testConnectionProperties.setupProperties();

        assertNotNull(testConnectionProperties.referencedComponent);

        assertNotNull(testConnectionProperties.host);
        assertEquals(EXPECTED_DEFAULT_HOST, testConnectionProperties.host.getStringValue());

        assertNotNull(testConnectionProperties.port);
        assertEquals(EXPECTED_DEFAULT_PORT, testConnectionProperties.port.getValue());

        assertNotNull(testConnectionProperties.database);
        assertEquals(EXPECTED_DEFAULT_DATABASE, testConnectionProperties.database.getStringValue());

        assertNotNull(testConnectionProperties.username);
        assertNull(testConnectionProperties.username.getStringValue());

        assertNotNull(testConnectionProperties.password);
        assertNull(testConnectionProperties.password.getStringValue());

        assertNotNull(testConnectionProperties.authentication);
        assertEquals(expectedDefaultAuthentication, testConnectionProperties.authentication.getStringValue());
    }

    @Test
    public void testSetupLayout() {
        testConnectionProperties.setupLayout();
        Form main = testConnectionProperties.getForm(Form.MAIN);

        assertNotNull(main.getWidget(testConnectionProperties.host));
        assertNotNull(main.getWidget(testConnectionProperties.port));
        assertNotNull(main.getWidget(testConnectionProperties.database));
        assertNotNull(main.getWidget(testConnectionProperties.username));
        assertNotNull(main.getWidget(testConnectionProperties.password));
        assertNotNull(main.getWidget(testConnectionProperties.authentication));

        Form reference = testConnectionProperties.getForm(Form.REFERENCE);

        assertNotNull(reference.getWidget(testConnectionProperties.referencedComponent));
    }

    @Test
    public void testAfterReferencedComponent() {
        MarkLogicConnectionProperties someConnection = new MarkLogicConnectionProperties("someConnection");

        testConnectionProperties.init();
        testConnectionProperties.referencedComponent.setReference(someConnection);
        testConnectionProperties.referencedComponent.componentInstanceId.setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");
        testConnectionProperties.afterReferencedComponent();

        Form main = testConnectionProperties.getForm(Form.MAIN);
        assertFalse(main.getWidget(testConnectionProperties.host).isVisible());
        assertFalse(main.getWidget(testConnectionProperties.port).isVisible());
        assertFalse(main.getWidget(testConnectionProperties.database).isVisible());
        assertFalse(main.getWidget(testConnectionProperties.username).isVisible());
        assertFalse(main.getWidget(testConnectionProperties.password).isVisible());
        assertFalse(main.getWidget(testConnectionProperties.authentication).isVisible());
    }

    @Test
    public void testValidateConnection() {
        testConnectionProperties.init();
        SandboxedInstance sandbox = Mockito.mock(SandboxedInstance.class);
        PowerMockito.mockStatic(SandboxInstanceFactory.class);
        PowerMockito.when(SandboxInstanceFactory.createSandboxedInstance(Mockito.any(RuntimeInfo.class), Mockito.any(ClassLoader.class), Mockito.eq(false))).thenReturn(sandbox);
        DatastoreRuntime<MarkLogicConnectionProperties> datastore = Mockito.mock(DatastoreRuntime.class);
        Mockito.when(sandbox.getInstance()).thenReturn(datastore);
        Mockito.when(datastore.doHealthChecks(null)).thenReturn(Collections.singletonList(new ValidationResult(Result.ERROR)));

        ValidationResult vr = testConnectionProperties.validateTestConnection();
        assertEquals(Result.ERROR, vr.getStatus());
        assertFalse(testConnectionProperties.getForm(MarkLogicConnectionProperties.WIZARD).isAllowFinish());

        Mockito.when(datastore.doHealthChecks(null)).thenReturn(Collections.singletonList(new ValidationResult(Result.OK)));

        vr = testConnectionProperties.validateTestConnection();
        assertEquals(Result.OK, vr.getStatus());
        Assert.assertTrue(testConnectionProperties.getForm(MarkLogicConnectionProperties.WIZARD).isAllowFinish());
    }
}
