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

package org.talend.components.netsuite.v2016_2;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.netsuite.AbstractNetSuiteTestBase;
import org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.input.NsObjectInputTransducer;
import org.talend.components.netsuite.v2016_2.client.NetSuiteClientFactoryImpl;

import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.NetSuiteService;
import com.netsuite.webservices.v2016_2.platform.core.Record;
import com.netsuite.webservices.v2016_2.platform.core.RecordList;
import com.netsuite.webservices.v2016_2.platform.core.SearchResult;

import static org.talend.components.netsuite.v2016_2.NetSuitePortTypeMockAdapterImpl.createSuccessStatus;

/**
 *
 */
public abstract class MockTestHelper {

    public static NetSuiteWebServiceMockTestFixture<NetSuitePortType, NetSuitePortTypeMockAdapterImpl> createWebServiceMockTestFixture()
            throws Exception {

        return new NetSuiteWebServiceMockTestFixture(new NetSuiteWebServiceMockTestFixture.NetSuiteServiceFactory() {
            @Override
            public Object createService(URL endpointUrl) {
                return new NetSuiteService(endpointUrl, NetSuiteService.SERVICE);
            }
        }, NetSuiteClientFactoryImpl.INSTANCE, NetSuitePortType.class, NetSuitePortTypeMockAdapterImpl.class,
                "NetSuitePort_2016_2");
    }

    public static <T extends Record> List<SearchResult> makeRecordPages(List<T> recordList, int pageSize)
            throws Exception {

        int count = recordList.size();
        int totalPages = count / pageSize;
        if (count % pageSize != 0) {
            totalPages += 1;
        }

        String searchId = UUID.randomUUID().toString();

        List<SearchResult> pageResults = new ArrayList<>();
        SearchResult result = null;

        Iterator<T> recordIterator = recordList.iterator();

        while (recordIterator.hasNext() && count > 0) {
            T record = recordIterator.next();

            if (result == null) {
                result = new SearchResult();
                result.setSearchId(searchId);
                result.setTotalPages(totalPages);
                result.setTotalRecords(count);
                result.setPageIndex(pageResults.size() + 1);
                result.setPageSize(pageSize);
                result.setStatus(createSuccessStatus());
            }

            if (result.getRecordList() == null) {
                result.setRecordList(new RecordList());
            }
            result.getRecordList().getRecord().add(record);

            if (result.getRecordList().getRecord().size() == pageSize) {
                pageResults.add(result);
                result = null;
            }

            count--;
        }

        if (result != null) {
            pageResults.add(result);
        }

        return pageResults;
    }

    public static <T> List<IndexedRecord> makeIndexedRecords(
            NetSuiteClientService<?> clientService, Schema schema,
            AbstractNetSuiteTestBase.ObjectComposer<T> objectComposer, int count) throws Exception {

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, schema.getName());

        List<IndexedRecord> recordList = new ArrayList<>();

        while (count > 0) {
            T nsRecord = objectComposer.composeObject();

            IndexedRecord convertedRecord = transducer.read(nsRecord);
            Schema recordSchema = convertedRecord.getSchema();

            GenericRecord record = new GenericData.Record(recordSchema);
            for (Schema.Field field : schema.getFields()) {
                Object value = convertedRecord.get(field.pos());
                record.put(field.pos(), value);
            }

            recordList.add(record);

            count--;
        }

        return recordList;
    }

}
