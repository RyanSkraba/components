package org.talend.components.snowflake.runtime;

import java.io.IOException;
import java.sql.Connection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.utils.DriverManagerUtils;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

/**
 * Unit tests for {@link SnowflakeSink} class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DriverManagerUtils.class)
public class SnowflakeSinkTest {

    private SnowflakeSink sink;

    @Before
    public void setup() {
        sink = new SnowflakeSink();
        sink.initialize(null, new TSnowflakeOutputProperties("outputProperties"));
    }

    @Test
    public void testValidate() throws Exception {
        PowerMockito.mockStatic(DriverManagerUtils.class);
        Mockito.when(DriverManagerUtils.getConnection(Mockito.any(SnowflakeConnectionProperties.class)))
                .thenReturn(Mockito.mock(Connection.class));
        Assert.assertEquals(ValidationResult.Result.OK, sink.validate(null).getStatus());
    }

    @Test
    public void testValidateFailed() throws Exception {
        PowerMockito.mockStatic(DriverManagerUtils.class);
        Mockito.when(DriverManagerUtils.getConnection(Mockito.any(SnowflakeConnectionProperties.class)))
                .thenThrow(new IOException("Can't connect to Snowflake"));
        Assert.assertEquals(ValidationResult.Result.ERROR, sink.validate(null).getStatus());
    }

    @Test
    public void testValidateWrongPropertiesType() throws Exception {
        sink.properties = new SnowflakeConnectionProperties("connectionProperties");
        PowerMockito.mockStatic(DriverManagerUtils.class);
        Mockito.when(DriverManagerUtils.getConnection(Mockito.any(SnowflakeConnectionProperties.class)))
                .thenReturn(Mockito.mock(Connection.class));
        Assert.assertEquals(ValidationResult.Result.ERROR, sink.validate(null).getStatus());

    }

    @Test
    public void testCreateWriteOperation() {
        WriteOperation<?> writer = sink.createWriteOperation();
        Assert.assertTrue(writer instanceof SnowflakeWriteOperation);
        Assert.assertEquals(sink, writer.getSink());
    }

    @Test
    public void testGetSnoflakeOuputProperties() {
        Assert.assertNotNull(sink.getSnowflakeOutputProperties());
    }

    @Test
    public void testI18NMessages() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeSink.class);
        String wrongPropertiesTypeMessage = i18nMessages.getMessage("debug.wrongPropertiesType");

        Assert.assertFalse(wrongPropertiesTypeMessage.equals("debug.wrongPropertiesType"));
    }
}
