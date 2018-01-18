package org.talend.components.marklogic.runtime;

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
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
    public void testPrepareMlcpCommandWithAllProperties() {
        initConnectionParameters();
        String expectedPrefix = "/loaded/";
        String expectedAdditionalParameter = "-content_encoding UTF-8 -a \"b c\"";

        bulkLoadProperties.docidPrefix.setValue(expectedPrefix);
        bulkLoadProperties.mlcpParams.setValue(expectedAdditionalParameter);

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRuntime.prepareMlcpCommandArray();

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
        String expectedReplaceValue = "\"/" + bulkLoadProperties.loadFolder.getStringValue()
                + ",'" + bulkLoadProperties.docidPrefix.getStringValue().substring(0, bulkLoadProperties.docidPrefix.getValue().length() - 1) + "'\"";
        assertThat(mlcpCommandArray, hasItemInArray(expectedReplaceValue));

        assertThat(mlcpCommandArray, hasItemInArray("-content_encoding"));
        assertThat(mlcpCommandArray, hasItemInArray("UTF-8"));
        assertThat(mlcpCommandArray, hasItemInArray("-a"));
        assertThat(mlcpCommandArray, hasItemInArray("\"b c\""));
    }

    @Test
    public void testPrepareMlcpCommandWithRequiredProperties() {
        initConnectionParameters();
        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRuntime.prepareMlcpCommandArray();

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
    public void testPrepareMlcpCommandWithReferencedConnection() {
        initConnectionParameters();
        bulkLoadProperties.connection.referencedComponent.setReference(connectionProperties);
        bulkLoadProperties.connection.referencedComponent.componentInstanceId.setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");

        bulkLoadRuntime.initialize(null, bulkLoadProperties);
        String[] mlcpCommandArray = bulkLoadRuntime.prepareMlcpCommandArray();


        assertThat(mlcpCommandArray, hasItemInArray("-host"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().host.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-port"));
        assertThat(mlcpCommandArray, hasItemInArray("" + bulkLoadProperties.connection.referencedComponent.getReference().port.getValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-database"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().database.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-username"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().username.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-password"));
        assertThat(mlcpCommandArray, hasItemInArray(bulkLoadProperties.connection.referencedComponent.getReference().password.getStringValue()));
        assertThat(mlcpCommandArray, hasItemInArray("-input_file_path"));
        assertThat(mlcpCommandArray, hasItemInArray("/" + bulkLoadProperties.loadFolder.getStringValue()));
    }
    @Test
    public void testPrepareMLCPCommandWithSpacesInFolder() {
        initConnectionParameters();
        bulkLoadProperties.connection.referencedComponent.setReference(connectionProperties);
        bulkLoadProperties.connection.referencedComponent.componentInstanceId.setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");

        bulkLoadProperties.loadFolder.setValue("a b");

        bulkLoadRuntime.initialize(null, bulkLoadProperties);

        String[] mlcpArray = bulkLoadRuntime.prepareMlcpCommandArray();
        assertThat(mlcpArray, hasItemInArray("-input_file_path"));
        assertThat(mlcpArray, hasItemInArray("a b"));
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
