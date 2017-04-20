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
import java.util.ArrayList;
import java.util.Collection;
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
import org.talend.components.netsuite.SchemaCustomMetaDataSource;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.ResultSet;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.search.SearchCondition;
import org.talend.components.netsuite.client.search.SearchQuery;

/**
 *
 */
public class NetSuiteSearchInputReader extends AbstractBoundedReader<IndexedRecord> {

    private transient NetSuiteClientService<?> clientService;
    private transient MetaDataSource metaDataSource;

    private transient NsObjectInputTransducer transducer;

    private transient Schema schema;

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
            schema = properties.module.main.schema.getValue();

            clientService = ((NetSuiteSource) getCurrentSource()).getClientService();

            MetaDataSource originalMetaDataSource = clientService.getMetaDataSource();
            metaDataSource = clientService.createDefaultMetaDataSource();
            metaDataSource.setCustomizationEnabled(originalMetaDataSource.isCustomizationEnabled());
            SchemaCustomMetaDataSource schemaCustomMetaDataSource = new SchemaCustomMetaDataSource(
                    clientService.getBasicMetaData(), originalMetaDataSource.getCustomMetaDataSource(), schema);
            metaDataSource.setCustomMetaDataSource(schemaCustomMetaDataSource);

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
        SearchQuery search = buildSearchQuery();

        RecordTypeInfo recordTypeInfo = search.getRecordTypeInfo();

        transducer = new NsObjectInputTransducer(clientService, schema, recordTypeInfo.getName());
        transducer.setMetaDataSource(metaDataSource);

        ResultSet<?> resultSet = search.search();
        return resultSet;
    }

    protected SearchQuery buildSearchQuery() {
        String target = properties.module.moduleName.getStringValue();

        SearchQuery search = clientService.newSearch(metaDataSource);
        search.target(target);

        List<String> fieldNames = properties.module.searchQuery.field.getValue();
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String operator = properties.module.searchQuery.operator.getValue().get(i);
                Object value1 = properties.module.searchQuery.value1.getValue().get(i);
                Object value2 = properties.module.searchQuery.value2.getValue().get(i);
                search.condition(buildSearchCondition(fieldName, operator, value1, value2));
            }
        }

        return search;
    }

    protected SearchCondition buildSearchCondition(String fieldName, String operator, Object value1, Object value2) {
        List<String> values = buildSearchConditionValueList(value1, value2);
        return new SearchCondition(fieldName, operator, values);
    }

    protected List<String> buildSearchConditionValueList(Object value1, Object value2) {
        if (value1 == null) {
            return null;
        }

        List<String> valueList;
        if (value1 instanceof Collection) {
            Collection<?> elements = (Collection<?>) value1;
            valueList = new ArrayList<>(elements.size());
            for (Object elemValue : elements) {
                if (elemValue != null) {
                    valueList.add(elemValue.toString());
                }
            }
        } else {
            valueList = new ArrayList<>(2);
            String sValue1 = value1 != null ? value1.toString() : null;
            if (StringUtils.isNotEmpty(sValue1)) {
                valueList.add(sValue1);

                String sValue2 = value2 != null ? value2.toString() : null;
                if (StringUtils.isNotEmpty(sValue2)) {
                    valueList.add(sValue2);
                }
            }
        }

        return valueList;
    }

    protected IndexedRecord transduceRecord(Object record) throws IOException {
        return transducer.read(record);
    }
}
