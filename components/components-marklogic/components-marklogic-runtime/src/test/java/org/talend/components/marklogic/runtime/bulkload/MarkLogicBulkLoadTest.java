//==============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================

package org.talend.components.marklogic.runtime.bulkload;

import com.marklogic.contentpump.ContentPump;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ContentPump.class)
public class MarkLogicBulkLoadTest {

    private MarkLogicBulkLoad bulkLoadRuntime;

    private MarkLogicConnectionProperties connectionProperties;

    private MarkLogicBulkLoadProperties bulkLoadProperties;

    @Before
    public void setUp() {
        bulkLoadRuntime = new MarkLogicBulkLoad();
        connectionProperties = new MarkLogicConnectionProperties("connectionProperties");
        bulkLoadProperties = new MarkLogicBulkLoadProperties("bulkLoadProperties");
    }

    private void initConnectionParameters() {
        String expectedHost = "someHost";
        Integer expectedPort = 8000;
        String expectedDatabase = "myDb";
        String expectedUserName = "myUser";
        String expectedPassword = "myPass";
        String expectedFolder = "D:/data/bulk_test";

        connectionProperties.init();
        connectionProperties.host.setValue(expectedHost);
        connectionProperties.port.setValue(expectedPort);
        connectionProperties.database.setValue(expectedDatabase);
        connectionProperties.username.setValue(expectedUserName);
        connectionProperties.password.setValue(expectedPassword);

        bulkLoadProperties.init();
        bulkLoadProperties.connection = connectionProperties;
        bulkLoadProperties.loadFolder.setValue(expectedFolder);
    }

    @Test
    public void testI18N() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarkLogicBulkLoad.class);
        assertFalse(i18nMessages.getMessage("messages.debug.command").equals("messages.debug.command"));
        assertFalse(i18nMessages.getMessage("messages.info.startBulkLoad").equals("messages.info.startBulkLoad"));
        assertFalse(i18nMessages.getMessage("messages.info.finishBulkLoad").equals("messages.info.finishBulkLoad"));
    }

    @Test
    public void testInitialize() {
        initConnectionParameters();
        ValidationResult vr = bulkLoadRuntime.initialize(null, bulkLoadProperties);
        assertEquals(ValidationResult.Result.OK, vr.getStatus());
    }

    @Test
    public void testInitializeWithEmptyProperties() {
        String emptyHost = "";
        Integer emptyPort = 0;
        String emptyDatabase = "";
        String emptyUserName = "";
        String emptyPassword = "";
        String emptyFolder = "";
        connectionProperties.init();
        connectionProperties.host.setValue(emptyHost);
        connectionProperties.port.setValue(emptyPort);
        connectionProperties.database.setValue(emptyDatabase);
        connectionProperties.username.setValue(emptyUserName);
        connectionProperties.password.setValue(emptyPassword);

        bulkLoadProperties.init();
        bulkLoadProperties.connection = connectionProperties;
        bulkLoadProperties.loadFolder.setValue(emptyFolder);

        ValidationResult vr = bulkLoadRuntime.initialize(null, bulkLoadProperties);
        assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
        assertNotNull(vr.getMessage());
    }

    @Test
    public void testInitializeWithWrongProperties() {
        initConnectionParameters();
        ValidationResult vr = bulkLoadRuntime.initialize(null, connectionProperties);
        assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
        assertNotNull(vr.getMessage());
    }

    @Test
    public void testRunAtDriver() throws Exception {
        initConnectionParameters();
        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        PowerMockito.mockStatic(ContentPump.class);
        Mockito.when(ContentPump.runCommand(any(String[].class))).thenReturn(0);
        bulkLoadRuntime.runAtDriver(null);
    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverWithException() throws Exception {
        initConnectionParameters();

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        PowerMockito.mockStatic(ContentPump.class);
        Mockito.when(ContentPump.runCommand(any(String[].class))).thenThrow(new IOException());
        bulkLoadRuntime.runAtDriver(null);
    }

}
