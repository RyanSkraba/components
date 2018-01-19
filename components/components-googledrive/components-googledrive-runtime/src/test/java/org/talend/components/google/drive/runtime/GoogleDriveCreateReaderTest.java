package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.google.drive.create.GoogleDriveCreateDefinition;
import org.talend.components.google.drive.create.GoogleDriveCreateProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;

public class GoogleDriveCreateReaderTest extends GoogleDriveTestBaseRuntime {

    protected GoogleDriveCreateProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveCreateProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveCreateProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        //
        properties.parentFolder.setValue(FOLDER_ROOT);
        properties.newFolder.setValue(FOLDER_CREATE);

        File fc = new File();
        fc.setId(FOLDER_CREATE_ID);
        when(drive.files().create(any(File.class)).setFields(eq("id")).execute()).thenReturn(fc);
    }

    @Test
    public void testStart() throws Exception {
        source.initialize(container, properties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(2, record.getSchema().getFields().size());
        assertEquals(FOLDER_ROOT, record.get(0));
        assertEquals(FOLDER_CREATE_ID, record.get(1));
        reader.close();
        Map<String, Object> returnValues = reader.getReturnValues();
        assertNotNull(returnValues);
        assertEquals(FOLDER_ROOT, returnValues.get(GoogleDriveCreateDefinition.RETURN_PARENT_FOLDER_ID));
        assertEquals(FOLDER_CREATE_ID, returnValues.get(GoogleDriveCreateDefinition.RETURN_NEW_FOLDER_ID));
    }

    @Test
    public void testValidationOK() throws Exception {
        ValidationResult vr = source.initialize(container, properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
    }

    @Test
    public void testValidationParentFolder() throws Exception {
        properties.parentFolder.setValue("");
        source.initialize(container, properties);
        assertEquals(Result.ERROR, source.validate(container).getStatus());
    }

    @Test
    public void testValidationNewFolder() throws Exception {
        properties.newFolder.setValue("");
        source.initialize(container, properties);
        assertEquals(Result.ERROR, source.validate(container).getStatus());
    }

}
