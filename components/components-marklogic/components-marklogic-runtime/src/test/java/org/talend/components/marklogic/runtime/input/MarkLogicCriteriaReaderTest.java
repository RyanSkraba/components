// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.marklogic.runtime.input;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.apache.avro.Schema;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;
import org.talend.daikon.avro.AvroUtils;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;

public class MarkLogicCriteriaReaderTest {

    @Test
    public void testStartWithoutConnection() throws IOException {
        MarkLogicSource mockedSource = mock(MarkLogicSource.class);
        when(mockedSource.connect(any(RuntimeContainer.class))).thenReturn(null);
        MarkLogicInputProperties properties = new MarkLogicInputProperties("inputProperties");
        properties.init();
        MarkLogicCriteriaReader criteriaReader = new MarkLogicCriteriaReader(mockedSource, null, properties);

        assertFalse(criteriaReader.start());
    }

    @Test
    public void testStartWithoutDocContentField() throws IOException {
        StringQueryDefinition mockedStringQueryDefinition = mock(StringQueryDefinition.class);
        QueryManager mockedQueryManager = mock(QueryManager.class);
        when(mockedQueryManager.newStringDefinition()).thenReturn(mockedStringQueryDefinition);
        when(mockedQueryManager.newStringDefinition(anyString())).thenReturn(mockedStringQueryDefinition);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        when(mockedClient.newDocumentManager()).thenReturn(null);
        when(mockedClient.newQueryManager()).thenReturn(mockedQueryManager);
        SearchHandle searchHandle = Mockito.mock(SearchHandle.class);
        Mockito.when(mockedQueryManager.search(Mockito.eq(mockedStringQueryDefinition), Mockito.any(SearchHandle.class)))
                .thenReturn(searchHandle);
        MarkLogicSource mockedSource = mock(MarkLogicSource.class);
        when(mockedSource.connect(any(RuntimeContainer.class))).thenReturn(mockedClient);

        MarkLogicInputProperties properties = new MarkLogicInputProperties("inputProperties");
        properties.init();
        Schema.Field docIdField = new Schema.Field("docId", AvroUtils._string(), null, (Object) null, Schema.Field.Order.IGNORE);
        properties.datasetProperties.main.schema.setValue(Schema.createRecord("docIdOnlySchema", null, null, false,
                Collections.singletonList(docIdField)));
        MarkLogicCriteriaReader criteriaReader = new MarkLogicCriteriaReader(mockedSource, null, properties);

        System.out.println(criteriaReader.start());

    }
}
