package org.talend.components.salesforce.runtime.dataprep;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.components.salesforce.datastore.SalesforceDatastoreProperties.ENDPOINT_PROPERTY_KEY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Test;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;

/**
 * The aim is to test SalesforceDatastoreProperties endpoint from a runtime context.
 */
public class SalesforceDataprepSourceTest {

    private static final String DUMMY_URL = "http://www.google.fr";

    @After
    public void tearDown() {
        System.clearProperty(SalesforceDatastoreProperties.CONFIG_FILE_lOCATION_KEY);
    }

    @Test
    public void testIfDatastoreDefinitionConfigurationIsAccessibleFromRuntime() {
        SalesforceDatastoreProperties salesforceDatastoreProperties = new SalesforceDatastoreProperties("");
        String endpoint = salesforceDatastoreProperties.getEndPoint();
        assertEquals(SalesforceConnectionProperties.URL, endpoint);
    }

    @Test
    public void testIfDatastoreEndpointCanBeLoadedFromPropertiesFile() throws IOException {
        initDatastorePropertiesFile(DUMMY_URL);
        SalesforceDatastoreProperties salesforceDatastoreProperties = new SalesforceDatastoreProperties("");
        String endpoint = salesforceDatastoreProperties.getEndPoint();
        assertEquals(DUMMY_URL, endpoint);

    }

    @Test
    public void testSalesforceDataprepSourceInitializeCall() throws IOException {
        initDatastorePropertiesFile(DUMMY_URL);

        SalesforceDataprepSource sfDataprepSource = new SalesforceDataprepSource();
        SalesforceInputProperties sfInputProperties = mock(SalesforceInputProperties.class);
        SalesforceDatasetProperties sfDatasetProperties = mock(SalesforceDatasetProperties.class);
        when(sfInputProperties.getDatasetProperties()).thenReturn(sfDatasetProperties);
        SalesforceDatastoreProperties sfDatastoreProperties = new SalesforceDatastoreProperties("");
        when(sfDatasetProperties.getDatastoreProperties()).thenReturn(sfDatastoreProperties);

        sfDataprepSource.initialize(null, sfInputProperties);
        assertEquals(DUMMY_URL, sfDatastoreProperties.getEndPoint());

    }

    private void initDatastorePropertiesFile(String url) throws IOException {
        File file = File.createTempFile("tempDatastore", "properties");
        String propValue = ENDPOINT_PROPERTY_KEY + "=" + url;
        Files.write(file.toPath(), propValue.getBytes());
        System.setProperty(SalesforceDatastoreProperties.CONFIG_FILE_lOCATION_KEY, file.getPath());
    }
}
