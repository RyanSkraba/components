package org.talend.components.snowflake.test;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.SnowflakeRowStandalone;
import org.talend.components.snowflake.tsnowflakeconnection.AuthenticationType;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class SnowflakeConnectionKeyPairIT extends SnowflakeTestIT {

    protected static final String KEY_STORE_LOCATION = System.getProperty("snowflake.keyStorePath");

    protected static final String KEY_STORE_TYPE = System.getProperty("snowflake.keyStoreType");

    protected static final String KEY_STORE_PASS_PHRASE = System.getProperty("snowflake.keyStorePassPhrase");

    protected static final String KEY_ALIAS = System.getProperty("snowflake.keyAlias");

    private static TestRuntimeContainer container;

    private static SnowflakeRowStandalone rowStandalone;

    private static TSnowflakeRowProperties rowProperties;

    @BeforeClass
    public static void setupClass() {
        container = new TestRuntimeContainer();
        rowProperties = new TSnowflakeRowProperties("row");
        rowProperties.dieOnError.setValue(true);
        rowStandalone = new SnowflakeRowStandalone();

        rowProperties.connection = (SnowflakeConnectionProperties) setupProps(null);
        rowProperties.connection.authenticationType.setValue(AuthenticationType.KEY_PAIR);
        rowProperties.connection.loginTimeout.setStoredValue(1);
        rowProperties.connection.userPassword.password.setValue(null);
        rowProperties.query.setValue("SELECT 1;");
    }

    @Before
    public void setSystemProperties() {
        System.setProperty("javax.net.ssl.keyStore", KEY_STORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStoreType", KEY_STORE_TYPE);
        System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASS_PHRASE);
    }

    @After
    public void resetSystemProperties() {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStoreType");
        System.clearProperty("javax.net.ssl.keyStorePassword");
    }

    @Ignore
    @Test
    public void testKeyPairLoginSuccessful() {
        rowProperties.connection.keyAlias.setValue(KEY_ALIAS);
        prepareAndRun();
        Assert.assertEquals(1, container.getComponentData(container.getCurrentComponentId(), "NB_LINE"));
    }

    @Test
    public void testKeyPairLoginWrongKeyStorePath() {
        System.setProperty("javax.net.ssl.keyStore", "wrong path");
        try {
            prepareAndRun();
        }catch (ComponentException ce) {
            Assert.assertEquals(IOException.class, ce.getCause().getClass());
        }
    }

    @Test
    public void testKeyPairLoginWrongPassPhrase() {
        System.setProperty("javax.net.ssl.keyStorePassword", "");

        try {
            prepareAndRun();
        }catch (ComponentException ce) {
            Assert.assertEquals(IOException.class, ce.getCause().getClass());
        }
    }

    @Test
    public void testKeyPairLoginWrongAlias() {
        rowProperties.connection.keyAlias.setValue("wrong one");
        try {
            prepareAndRun();
        }catch (ComponentException ce) {
            Assert.assertEquals(IOException.class, ce.getCause().getClass());
        }
    }

    private void prepareAndRun() {
        rowStandalone.initialize(container, rowProperties);
        rowStandalone.runAtDriver(container);
    }

    @Override
    public DefinitionRegistryService getDefinitionService() {
        DefinitionRegistry definitionRegistry = new DefinitionRegistry();
        definitionRegistry.registerDefinition(Collections.singleton(new TSnowflakeConnectionDefinition()));
        return definitionRegistry;
    }
}
