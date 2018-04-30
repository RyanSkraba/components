package org.talend.components.google.drive.runtime.data;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.google.drive.runtime.GoogleDriveUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.User;

public class GoogleDriveDataSourceTest extends GoogleDriveDataBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testInitialize() throws Exception {
        assertEquals(Result.OK, dataSource.initialize(container, inputProperties).getStatus());
    }

    @Test
    public void testValidate() throws Exception {
        inputProperties.getDatasetProperties().getDatastoreProperties().serviceAccountJSONFile.setValue("");
        dataSource.initialize(container, inputProperties);
        assertEquals(Result.ERROR, dataSource.validate(container).getStatus());

        dataSource = spy(dataSource);
        Drive drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        GoogleDriveUtils utils = mock(GoogleDriveUtils.class, RETURNS_DEEP_STUBS);
        doReturn(drive).when(dataSource).getDriveService();
        doReturn(utils).when(dataSource).getDriveUtils();
        inputProperties.getDatasetProperties().getDatastoreProperties().serviceAccountJSONFile.setValue("service.json");
        dataSource.initialize(container, inputProperties);

        About about = new About();
        User user = new User();
        user.setEmailAddress("test@example.org");
        about.setUser(user);
        when(drive.about().get().setFields(anyString()).execute()).thenReturn(about);
        assertEquals(Result.OK, dataSource.validate(container).getStatus());
    }

    @Test
    public void testCreateReader() throws Exception {
        dataSource = spy(dataSource);
        Drive drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        GoogleDriveUtils utils = mock(GoogleDriveUtils.class, RETURNS_DEEP_STUBS);
        doReturn(drive).when(dataSource).getDriveService();
        doReturn(utils).when(dataSource).getDriveUtils();

        dataSource.initialize(container, inputProperties);
        BoundedReader reader = dataSource.createReader(container);
        assertNotNull(reader);
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        List<NamedThing> schemas = dataSource.getSchemaNames(container);
        assertEquals(1, schemas.size());
        assertEquals("GoogleDriveList", schemas.get(0).getName());
    }

    @Test
    public void testGetEndpointSchema() throws Exception {
        dataSource.initialize(container, inputProperties);
        assertEquals(datasetSchema, dataSource.getEndpointSchema(container, "test"));
    }

    @Test
    public void testSplitIntoBundles() throws Exception {
        List<? extends BoundedSource> bundles = dataSource.splitIntoBundles(10, container);
        assertEquals(1, bundles.size());
        assertEquals(dataSource, bundles.get(0));
    }

    @Test
    public void testGetEstimatedSizeBytes() throws Exception {
        assertEquals(0, dataSource.getEstimatedSizeBytes(container));
    }

    @Test
    public void testProducesSortedKeys() throws Exception {
        assertFalse(dataSource.producesSortedKeys(container));
    }
}
