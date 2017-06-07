package org.talend.components.service.rest.impl;

import static org.junit.Assert.assertEquals;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatasetWritePayloadTest {

    @Test
    public void readData() throws Exception {
        DatasetWritePayload datasetWritePayload = DatasetWritePayload.readData(
                getClass().getResourceAsStream("/org/talend/components/service/rest/jdbc_component_write_properties_on_DI.json"),
                new ObjectMapper());

        IndexedRecord indexedRecord = datasetWritePayload.getData().next();
        assertEquals("1", indexedRecord.get(0).toString());
        assertEquals("David", indexedRecord.get(1).toString());
        assertEquals("Bowie", indexedRecord.get(2).toString());
        assertEquals("david.bowie@awesome.uk", indexedRecord.get(3).toString());
        assertEquals("male", indexedRecord.get(4).toString());
        assertEquals("127.0.0.1", indexedRecord.get(5).toString());
        
    }

}