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
package org.talend.components.google.drive.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.google.drive.get.GoogleDriveGetProperties;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode;
import org.talend.daikon.avro.AvroUtils;

public class GoogleDrivePutGetTestIT extends GoogleDriveBaseTestIT {

    private GoogleDrivePutProperties putProperties;

    private GoogleDriveGetProperties getProperties;

    private String folder;

    private String file = "testFile.txt";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = "TalendTest_putget" + testTS;
        putProperties = new GoogleDrivePutProperties(TEST_NAME);
        putProperties.setupProperties();
        putProperties.connection = connectionProperties;
        putProperties.overwrite.setValue(true);
        putProperties.uploadMode.setValue(UploadMode.READ_CONTENT_FROM_INPUT);
        putProperties.destinationFolder.setValue(folder);
        putProperties.fileName.setValue(file);

        getProperties = new GoogleDriveGetProperties(TEST_NAME);
        getProperties.setupProperties();
        getProperties.connection = connectionProperties;
        getProperties.file.setValue("/" + folder + "/" + file);
    }

    @Test
    public void testPutGet() throws Exception {
        createFolderAtRoot(folder);
        // put file
        sink.initialize(container, putProperties);
        WriterWithFeedback writer = (WriterWithFeedback) sink.createWriteOperation().createWriter(container);
        writer.open(TEST_NAME);
        IndexedRecord record = new GenericData.Record(SchemaBuilder.builder().record("write").fields() //
                .name(GoogleDrivePutDefinition.RETURN_CONTENT).type(AvroUtils._bytes()).noDefault() //
                .endRecord());
        String content = "ABC\nDEF\nRH";
        record.put(0, content.getBytes());
        writer.write(record);
        writer.close();
        Iterator results = writer.getSuccessfulWrites().iterator();
        assertTrue(results.hasNext());
        IndexedRecord put = (IndexedRecord) results.next();
        assertNotNull(put);
        LOG.debug("put record = {}.", put);
        String putContent = new String((byte[]) put.get(0));
        assertEquals(content, putContent);
        // get file
        source.initialize(container, getProperties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord got = (IndexedRecord) reader.getCurrent();
        assertNotNull(got);
        LOG.debug("got record = {}.", got);
        String gotContent = new String((byte[]) got.get(0));
        assertEquals(content, gotContent);
    }
}
