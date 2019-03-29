// ============================================================================
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
// ============================================================================
package org.talend.components.snowflake.test;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.common.CommonTestUtils;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeFamilyDefinition;
import org.talend.components.snowflake.SnowflakeRegion;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;

@SuppressWarnings("nls")
public abstract class SnowflakeTestIT extends AbstractComponentTest {
    @ClassRule
    public static final TestRule DISABLE_IF_NEEDED = new DisableIfMissingConfig();

    @Inject
    private DefinitionRegistryService definitionService;

    protected static Connection testConnection;

    private DefinitionRegistry testComponentRegistry;

    protected static final String ACCOUNT_STR = System.getProperty("snowflake.account");

    protected static final SnowflakeRegion SNOWFLAKE_REGION = SnowflakeRegion.AWS_US_WEST;

    protected static final String USER = System.getProperty("snowflake.user");

    protected static final String PASSWORD = System.getProperty("snowflake.password");

    protected static final String WAREHOUSE = System.getProperty("snowflake.warehouse");

    protected static final String SCHEMA = System.getProperty("snowflake.schema");

    protected static final String DB = System.getProperty("snowflake.db");

    protected static final String TEST_TABLE = "LOADER_TEST_TABLE";

    // So that multiple tests can run at the same time
    protected static String testNumber = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));

    protected static String testTable = TEST_TABLE + "_" + testNumber;

    protected static String testSchema = SCHEMA + "_" + testNumber;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    protected static final int NUM_COLUMNS = 8;

    public SnowflakeTestIT() {
    }

    public static ComponentProperties setupProps(SnowflakeConnectionProperties props) {
        if (props == null) {
            props = (SnowflakeConnectionProperties) new SnowflakeConnectionProperties("foo").init();
        }
        props.userPassword.userId.setStoredValue(USER);
        props.userPassword.password.setStoredValue(PASSWORD);
        props.account.setStoredValue(ACCOUNT_STR);
        props.region.setStoredValue(SNOWFLAKE_REGION);
        props.warehouse.setStoredValue(WAREHOUSE);
        props.db.setStoredValue(DB);
        props.schemaName.setStoredValue(testSchema);
        return props;
    }

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    @Override
    public ComponentService getComponentService() {
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();
            // register component
            testComponentRegistry.registerComponentFamilyDefinition(new SnowflakeFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void generateJavaNestedCompPropClassNames() {
        Set<ComponentDefinition> allComponents = getComponentService().getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            ComponentProperties props = cd.createProperties();
            String javaCode = PropertiesTestUtils.generatedNestedComponentCompatibilitiesJavaCode(props);
            LOGGER.debug("Nested Props for (" + cd.getClass().getSimpleName() + ".java:1)" + javaCode);
        }
    }

    @Test
    public void checkConnectorsSchema() {
        CommonTestUtils.checkAllSchemaPathAreSchemaTypes(getComponentService(), errorCollector);
    }

    @Override
    @Test
    public void testAlli18n() {
        PropertiesTestUtils.assertAlli18nAreSetup(getDefinitionService(), errorCollector);
    }

    protected DefinitionRegistryService getDefinitionService() {
        return definitionService;
    }

    static class RepoProps {

        org.talend.daikon.properties.Properties props;

        String name;

        String repoLocation;

        Schema schema;

        String schemaPropertyName;

        RepoProps(org.talend.daikon.properties.Properties props, String name, String repoLocation, String schemaPropertyName) {
            this.props = props;
            this.name = name;
            this.repoLocation = repoLocation;
            this.schemaPropertyName = schemaPropertyName;
            if (schemaPropertyName != null) {
                this.schema = (Schema) props.getValuedProperty(schemaPropertyName).getValue();
            }
        }

        @Override
        public String toString() {
            return "RepoProps: " + repoLocation + "/" + name + " props: " + props;
        }
    }

    class TestRepository implements Repository {

        private int locationNum;

        public String componentIdToCheck;

        public ComponentProperties properties;

        public List<RepoProps> repoProps;

        TestRepository(List<RepoProps> repoProps) {
            this.repoProps = repoProps;
        }

        @Override
        public String storeProperties(org.talend.daikon.properties.Properties properties, String name, String repositoryLocation,
                String schemaPropertyName) {
            RepoProps rp = new RepoProps(properties, name, repositoryLocation, schemaPropertyName);
            repoProps.add(rp);
            LOGGER.debug(rp.toString());
            return repositoryLocation + ++locationNum;
        }
    }

    static class TestRuntimeContainer extends DefaultComponentRuntimeContainerImpl {
    }

}
