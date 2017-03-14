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

package org.talend.components.netsuite.input;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.NetSuiteSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.search.SearchCondition;
import org.talend.components.netsuite.client.search.SearchQuery;
import org.talend.components.netsuite.client.ResultSet;

/**
 *
 */
public class NetSuiteSearchInputReader extends AbstractBoundedReader<IndexedRecord> {

    private transient NetSuiteClientService<?> clientService;

    private transient NsObjectInputTransducer transducer;

    private transient Schema searchSchema;

    private NetSuiteInputProperties properties;

    private int dataCount;

    private RuntimeContainer container;

    private ResultSet<?> resultSet;

    private Object currentRecord;
    private IndexedRecord currentIndexedRecord;

    public NetSuiteSearchInputReader(RuntimeContainer container,
            NetSuiteSource source, NetSuiteInputProperties properties) {
        super(source);

        this.container = container;
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        try {
            clientService = ((NetSuiteSource) getCurrentSource()).getClientService();
            resultSet = search();
            return advance();
        } catch (NetSuiteException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        try {
            if (resultSet.next()) {
                currentRecord = resultSet.get();
                currentIndexedRecord = transduceRecord(currentRecord);
                dataCount++;
                return true;
            }
            return false;
        } catch (NetSuiteException e) {
            throw new IOException(e);
        }
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return currentIndexedRecord;
    }

    @Override
    public void close() throws IOException {
        // Nothing to close
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.totalCount = dataCount;
        return result.toMap();
    }

    protected ResultSet<?> search() throws NetSuiteException {
        searchSchema = properties.module.main.schema.getValue();

        String target = properties.module.moduleName.getStringValue();

        SearchQuery search = clientService.newSearch();
        search.target(target);

        List<String> fieldNames = properties.module.searchQuery.field.getValue();
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String operator = properties.module.searchQuery.operator.getValue().get(i);
                String value1 = properties.module.searchQuery.value1.getValue().get(i);
                String value2 = properties.module.searchQuery.value2.getValue().get(i);
                List<String> values = null;
                if (StringUtils.isNotEmpty(value1)) {
                    values = StringUtils.isNotEmpty(value2) ? Arrays.asList(value1, value2) : Arrays.asList(value1);
                }
                search.condition(new SearchCondition(fieldName, operator, values));
            }
        }

        RecordTypeInfo recordTypeInfo = search.getRecordTypeInfo();
        transducer = new NsObjectInputTransducer(clientService, searchSchema, recordTypeInfo.getName());

        ResultSet<?> resultSet = search.search();
        return resultSet;
    }

    protected IndexedRecord transduceRecord(Object record) throws IOException {
        return transducer.read(record);
    }
}
