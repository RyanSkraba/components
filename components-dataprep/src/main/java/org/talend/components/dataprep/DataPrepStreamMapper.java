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
package org.talend.components.dataprep;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

class DataPrepStreamMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepStreamMapper.class);

    private ObjectMapper objectMapper;
    private JsonParser jsonParser;
    private MappingIterator<Map<String,String>> iterator;

    private DataPrepStreamMapper() {
        objectMapper = new ObjectMapper();
    }

    public DataPrepStreamMapper (InputStream inputStream) throws IOException {
        this();
        this.jsonParser = new JsonFactory().createParser(inputStream);
    }

    public boolean initIterator() throws IOException {
        JsonToken currentToken;
        while ((currentToken = jsonParser.nextToken()) != JsonToken.END_OBJECT) {
            if (currentToken == JsonToken.START_ARRAY) {
                jsonParser.nextToken();
                this.iterator = objectMapper.readValues(jsonParser, new TypeReference<Map<String, String>>() {
                });
                return true;
            }
        }
        return false;
    }

    Map<String, String> nextRecord() {
        Map<String, String> record = iterator.next();
        record.remove("tdpId");
        LOGGER.debug("Record is : {}", record);
        return record;
    }

    boolean hasNextRecord() {
        return iterator.hasNext();
    }

    void close() throws IOException {
        jsonParser.close();
    }

    MetaData getMetaData() throws IOException{
        return objectMapper.readValue(jsonParser, MetaData.class);
    }
}
