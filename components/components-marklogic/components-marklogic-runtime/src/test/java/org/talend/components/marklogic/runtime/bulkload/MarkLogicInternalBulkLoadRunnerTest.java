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

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertThat;

public class MarkLogicInternalBulkLoadRunnerTest {

    private MarkLogicBulkLoad bulkLoadRuntime;

    private AbstractMarkLogicBulkLoadRunner bulkLoadRunner;

    private MarkLogicConnectionProperties connectionProperties;

    private MarkLogicBulkLoadProperties bulkLoadProperties;

    @Before
    public void setUp() {
        connectionProperties = new MarkLogicConnectionProperties("connectionProperties");
        bulkLoadProperties = new MarkLogicBulkLoadProperties("bulkLoadProperties");
        bulkLoadRuntime = new MarkLogicBulkLoad();
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
    public void testPrepareMlcpCommandWithRequiredProperties() {
        initConnectionParameters();
        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        bulkLoadRunner = new MarkLogicInternalBulkLoadRunner(bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRunner.prepareMLCPCommand();

        assertThat(mlcpCommandArray, hasItemInArray("-host"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.host.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-port"));
        assertThat(mlcpCommandArray, hasItemInArray("" + bulkLoadProperties.connection.port.getValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-database"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.database.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-username"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.username.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-password"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.password.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-input_file_path"));
        assertThat(mlcpCommandArray, hasItemInArray("/" + bulkLoadProperties.loadFolder.getStringValue()));
    }

    @Test
    public void testPrepareMlcpCommandWithAllProperties() {
        initConnectionParameters();
        String expectedPrefix = "/loaded/";
        String expectedAdditionalParameter = "-content_encoding UTF-8 -a \"b c\"";

        bulkLoadProperties.docidPrefix.setValue(expectedPrefix);
        bulkLoadProperties.mlcpParams.setValue(expectedAdditionalParameter);

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        bulkLoadRunner = new MarkLogicInternalBulkLoadRunner(bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRunner.prepareMLCPCommand();

        assertThat(mlcpCommandArray, hasItemInArray("-host"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.host.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-port"));
        assertThat(mlcpCommandArray, hasItemInArray("" + bulkLoadProperties.connection.port.getValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-database"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.database.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-username"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.username.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-password"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.password.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-input_file_path"));
        assertThat(mlcpCommandArray, hasItemInArray("/" + bulkLoadProperties.loadFolder.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-output_uri_replace"));
        String expectedReplaceValue =
                "\"/" + bulkLoadProperties.loadFolder.getStringValue() + ",'" + bulkLoadProperties.docidPrefix.getStringValue()
                        .substring(0, bulkLoadProperties.docidPrefix.getValue().length() - 1) + "'\"";
        assertThat(mlcpCommandArray, hasItemInArray(expectedReplaceValue));

        assertThat(mlcpCommandArray, hasItemInArray("-content_encoding"));
        assertThat(mlcpCommandArray, hasItemInArray("UTF-8"));
        assertThat(mlcpCommandArray, hasItemInArray("-a"));
        assertThat(mlcpCommandArray, hasItemInArray("\"b c\""));
    }

    @Test
    public void testPrepareMLCPCommandWithSpacesInFolder() {
        initConnectionParameters();
        bulkLoadProperties.connection.referencedComponent.setReference(connectionProperties);
        bulkLoadProperties.connection.referencedComponent.componentInstanceId
                .setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");

        bulkLoadProperties.loadFolder.setValue("a b");

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        bulkLoadRunner = new MarkLogicInternalBulkLoadRunner(bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRunner.prepareMLCPCommand();

        assertThat(mlcpCommandArray, hasItemInArray("-input_file_path"));
        assertThat(mlcpCommandArray, hasItemInArray("a b"));
    }

    @Test
    public void testPrepareMlcpCommandWithReferencedConnection() {
        initConnectionParameters();
        bulkLoadProperties.connection.referencedComponent.setReference(connectionProperties);
        bulkLoadProperties.connection.referencedComponent.componentInstanceId
                .setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        bulkLoadRunner = new MarkLogicInternalBulkLoadRunner(bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRunner.prepareMLCPCommand();

        assertThat(mlcpCommandArray, hasItemInArray("-host"));
        assertThat(mlcpCommandArray,
                hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().host.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-port"));
        assertThat(mlcpCommandArray,
                hasItemInArray("" + bulkLoadProperties.connection.referencedComponent.getReference().port.getValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-database"));
        assertThat(mlcpCommandArray,
                hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().database.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-username"));
        assertThat(mlcpCommandArray,
                hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().username.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-password"));
        assertThat(mlcpCommandArray,
                hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().password.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-input_file_path"));
        assertThat(mlcpCommandArray, hasItemInArray("/" + bulkLoadProperties.loadFolder.getStringValue()));
    }
}