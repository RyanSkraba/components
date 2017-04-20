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

package org.talend.components.netsuite.avro.converter;

import java.io.IOException;

import org.apache.avro.Schema;
import org.talend.components.netsuite.NetSuiteErrorCode;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.exception.ExceptionContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Responsible for conversion of NetSuite <code>object</code> from/to <code>JSON</code>.
 */
public class ObjectToJsonConverter<T> implements AvroConverter<T, String> {

    private Class<T> clazz;

    private ObjectReader objectReader;

    private ObjectWriter objectWriter;

    public ObjectToJsonConverter(Class<T> clazz, ObjectMapper objectMapper) {
        this.clazz = clazz;

        objectWriter = objectMapper.writer().forType(clazz);
        objectReader = objectMapper.reader().forType(clazz);
    }

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<T> getDatumClass() {
        return clazz;
    }

    @Override
    public String convertToAvro(T value) {
        if (value == null) {
            return null;
        }
        try {
            return objectWriter.writeValueAsString(value);
        } catch (IOException e) {
            throw new NetSuiteException(new NetSuiteErrorCode("JSON_PROCESSING"), e,
                    ExceptionContext.build().put(ExceptionContext.KEY_MESSAGE, e.getMessage()));
        }
    }

    @Override
    public T convertToDatum(String value) {
        if (value == null) {
            return null;
        }
        try {
            return objectReader.readValue(value);
        } catch (IOException e) {
            throw new NetSuiteException(new NetSuiteErrorCode("JSON_PROCESSING"), e,
                    ExceptionContext.build().put(ExceptionContext.KEY_MESSAGE, e.getMessage()));
        }
    }
}
