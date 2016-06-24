// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataPrepStreamMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepStreamMapper.class);

    private ObjectMapper objectMapper;

    private JsonParser jsonParser;

    private MappingIterator<Map<String, String>> iterator;

    private DataPrepStreamMapper() {
        objectMapper = new ObjectMapper();
    }

    public DataPrepStreamMapper(InputStream inputStream) throws IOException {
        this();
        this.jsonParser = new JsonFactory().createParser(inputStream);
    }

    public boolean initIterator() throws IOException {
        boolean hasMetRecords = false;
        int level = 0;
        while (!hasMetRecords) {
            final JsonToken currentToken = jsonParser.nextToken();
            if (currentToken == null) {
                return false; // EOF
            }
            switch (currentToken) {
                case START_OBJECT:
                    level++;
                    break;
                case END_OBJECT:
                    level--;
                    break;
                case FIELD_NAME:
                    if ("records".equals(jsonParser.getText()) && level == 1) {
                        hasMetRecords = true;
                    }
                    break;
            }
        }
        // Create iterator to the records in "records" element
        if ("records".equals(jsonParser.getText())) {
            jsonParser.nextToken(); // Field
            jsonParser.nextToken(); // Start object
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                this.iterator = objectMapper.readValues(jsonParser, new TypeReference<Map<String, String>>() {
                });
                return true;
            }
        }

        return false;
    }

    public Map<String, String> nextRecord() {
        Map<String, String> record = iterator.next();
        record.remove("tdpId");
        LOGGER.debug("Record is : {}", record);
        return record;
    }

    public boolean hasNextRecord() {
        return iterator.hasNext();
    }

    public void close() throws IOException {
        jsonParser.close();
    }

    MetaData getMetaData() throws IOException {
        return objectMapper.readValue(jsonParser, MetaData.class);
    }
}
