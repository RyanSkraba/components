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
package org.talend.components.marketo.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.service.Repository;

public class MarketoCustomObjectsSchemasPropertiesTest {

    MarketoCustomObjectsSchemasProperties properties;

    TMarketoConnectionProperties connection;

    static class RepoProps {

        Properties props;

        String name;

        String repoLocation;

        Schema schema;

        String schemaPropertyName;

        RepoProps(Properties props, String name, String repoLocation, String schemaPropertyName) {
            this.props = props;
            this.name = name;
            this.repoLocation = repoLocation;
            this.schemaPropertyName = schemaPropertyName;
            if (schemaPropertyName != null) {
                this.schema = (Schema) props.getValuedProperty(schemaPropertyName).getValue();
            }
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
        public String storeProperties(Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
            RepoProps rp = new RepoProps(properties, name, repositoryLocation, schemaPropertyName);
            repoProps.add(rp);
            return repositoryLocation + ++locationNum;
        }
    }

    private final List<RepoProps> repoProps = new ArrayList<>();

    private Repository repo = new TestRepository(repoProps);

    @Before
    public void setUp() throws Exception {
        properties = new MarketoCustomObjectsSchemasProperties("test");
        connection = new TMarketoConnectionProperties("testconn");
        connection.setupProperties();
        connection.apiMode.setValue(APIMode.REST);
        connection.endpoint.setValue("http://fakeendpoint.com");
        connection.clientAccessId.setValue("user0000");
        connection.secretKey.setValue("secretk");
    }

    @Test
    public void testGetConnectionProperties() throws Exception {
        assertNotNull(properties.getConnectionProperties());
    }

    @Test
    public void testSetConnection() throws Exception {
        assertEquals(properties, properties.setConnection(connection));
        assertEquals(connection, properties.getConnectionProperties());
    }

    @Test
    public void testSetRepositoryLocation() throws Exception {
        properties.setRepositoryLocation("___DRI");
        assertEquals("___DRI", properties.getRepositoryLocation());
    }

    @Test(expected = NullPointerException.class)
    public void testBeforeFormPresentCustomObjects() throws Exception {
        properties.beforeFormPresentCustomObjects();
        fail("Shouldn't be here");
    }

    @Test(expected = NullPointerException.class)
    public void testAfterFormFinishCustomObjects() throws Exception {
        List<NamedThing> o = new ArrayList<>();
        o.add(new SimpleNamedThing("name", "displayName."));
        properties.selectedCustomObjectsNames.setValue(o);
        properties.afterFormFinishCustomObjects(repo);
        // TODO avoid NPE.
    }

}
