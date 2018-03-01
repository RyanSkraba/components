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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.components.marklogic.util.CommandExecutor;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommandExecutor.class)
public class MarkLogicExternalBulkLoadRunnerTest {

    private MarkLogicBulkLoad bulkLoadRuntime;

    private MarkLogicConnectionProperties connectionProperties;

    private MarkLogicBulkLoadProperties bulkLoadProperties;

    private MarkLogicExternalBulkLoadRunner bulkLoadRunner;

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
        bulkLoadProperties.useExternalMLCP.setValue(true);
    }

    @Test
    public void testPrepareMlcpCommandWithAllProperties() {
        initConnectionParameters();
        String expectedPrefix = "/loaded/";
        String expectedAdditionalParameter = "-content_encoding UTF-8";

        bulkLoadProperties.docidPrefix.setValue(expectedPrefix);
        bulkLoadProperties.mlcpParams.setValue(expectedAdditionalParameter);

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        bulkLoadRunner = new MarkLogicExternalBulkLoadRunner(bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRunner.prepareMLCPCommand();
        String actualMlcpCommand = bulkLoadRunner.prepareMLCPCommandCMD(mlcpCommandArray);

        assertThat(actualMlcpCommand, containsString("-host " + bulkLoadProperties.connection.host.getStringValue()));
        assertThat(actualMlcpCommand, containsString("-port " + bulkLoadProperties.connection.port.getValue()));
        assertThat(actualMlcpCommand, containsString("-database " + bulkLoadProperties.connection.database.getStringValue()));
        assertThat(actualMlcpCommand, containsString("-username " + bulkLoadProperties.connection.username.getStringValue()));
        assertThat(actualMlcpCommand, containsString("-password " + bulkLoadProperties.connection.password.getStringValue()));
        assertThat(actualMlcpCommand, containsString("-input_file_path " + "/" + bulkLoadProperties.loadFolder.getStringValue()));
        assertThat(actualMlcpCommand, containsString(
                "-output_uri_replace " + "\"/" + bulkLoadProperties.loadFolder.getStringValue() + ",'"
                        + bulkLoadProperties.docidPrefix.getStringValue()
                        .substring(0, bulkLoadProperties.docidPrefix.getStringValue().length() - 1) + "'\""));
        assertThat(actualMlcpCommand, containsString(bulkLoadProperties.mlcpParams.getStringValue()));
    }

    @Test
    public void testPrepareMlcpCommandWithRequiredProperties() {
        initConnectionParameters();
        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        bulkLoadRunner = new MarkLogicExternalBulkLoadRunner(bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRunner.prepareMLCPCommand();
        String mlcpCommand = bulkLoadRunner.prepareMLCPCommandCMD(mlcpCommandArray);

        assertThat(mlcpCommand, containsString("-host " + bulkLoadProperties.connection.host.getStringValue()));
        assertThat(mlcpCommand, containsString("-port " + bulkLoadProperties.connection.port.getValue()));
        assertThat(mlcpCommand, containsString("-database " + bulkLoadProperties.connection.database.getStringValue()));
        assertThat(mlcpCommand, containsString("-username " + bulkLoadProperties.connection.username.getStringValue()));
        assertThat(mlcpCommand, containsString("-password " + bulkLoadProperties.connection.password.getStringValue()));
        assertThat(mlcpCommand, containsString("-input_file_path " + "/" + bulkLoadProperties.loadFolder.getStringValue()));
    }

    @Test
    public void testPrepareMlcpCommandWithReferencedConnection() {
        initConnectionParameters();
        bulkLoadProperties.connection.referencedComponent.setReference(connectionProperties);
        bulkLoadProperties.connection.referencedComponent.componentInstanceId
                .setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        bulkLoadRunner = new MarkLogicExternalBulkLoadRunner(bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRunner.prepareMLCPCommand();
        String mlcpCommand = bulkLoadRunner.prepareMLCPCommandCMD(mlcpCommandArray);

        assertThat(mlcpCommand, containsString(
                "-host " + bulkLoadProperties.connection.referencedComponent.getReference().host.getStringValue()));
        assertThat(mlcpCommand,
                containsString("-port " + bulkLoadProperties.connection.referencedComponent.getReference().port.getValue()));
        assertThat(mlcpCommand, containsString(
                "-database " + bulkLoadProperties.connection.referencedComponent.getReference().database.getStringValue()));
        assertThat(mlcpCommand, containsString(
                "-username " + bulkLoadProperties.connection.referencedComponent.getReference().username.getStringValue()));
        assertThat(mlcpCommand, containsString(
                "-password " + bulkLoadProperties.connection.referencedComponent.getReference().password.getStringValue()));
        assertThat(mlcpCommand, containsString("-input_file_path " + "/" + bulkLoadProperties.loadFolder.getStringValue()));
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
    public void testMlcpCommandStart() {
        bulkLoadRunner = new MarkLogicExternalBulkLoadRunner(bulkLoadProperties);
        String windowsCommandStart = bulkLoadRunner.prepareMlcpCommandStart("Windows VERSION");
        String anotherCommandStart = bulkLoadRunner.prepareMlcpCommandStart("product of Linus Torvalds");

        assertThat(windowsCommandStart, Matchers.equalTo("cmd /c mlcp.bat "));
        assertThat(anotherCommandStart, Matchers.equalTo("mlcp.sh "));
    }

    @Test
    public void testRunAtDriver() throws Exception {
        initConnectionParameters();
        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        Process process = Mockito.mock(Process.class);
        InputStream mockedInputStream = Mockito.mock(InputStream.class);
        Mockito.when(mockedInputStream.available()).thenReturn(0);
        PowerMockito.mockStatic(CommandExecutor.class);
        Mockito.when(CommandExecutor.executeCommand(anyString())).thenReturn(process);
        Mockito.when(process.getInputStream()).thenReturn(mockedInputStream);
        Mockito.when(process.getErrorStream()).thenReturn(mockedInputStream);
        bulkLoadRuntime.runAtDriver(null);
        Mockito.verify(process).waitFor();
    }

    @Test
    public void testRunAtDriverWithIOException() throws Exception {
        initConnectionParameters();

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        Process process = Mockito.mock(Process.class);
        InputStream mockedInputStream = Mockito.mock(InputStream.class);
        Mockito.when(mockedInputStream.available()).thenReturn(1); //stream is available, but not readable
        PowerMockito.mockStatic(CommandExecutor.class);
        Mockito.when(CommandExecutor.executeCommand(anyString())).thenReturn(process);
        Mockito.when(process.getInputStream()).thenReturn(mockedInputStream);
        Mockito.when(process.getErrorStream()).thenReturn(mockedInputStream);
        bulkLoadRuntime.runAtDriver(null);

        Mockito.verify(process).waitFor();
    }

    @Test(expected = MarkLogicException.class)
    public void testRunAtDriverWithException() throws Exception {
        initConnectionParameters();

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        PowerMockito.mockStatic(CommandExecutor.class);
        Mockito.when(CommandExecutor.executeCommand(anyString())).thenThrow(new IOException());
        bulkLoadRuntime.runAtDriver(null);
    }

}