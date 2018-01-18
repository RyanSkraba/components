package org.talend.components.marklogic.data;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.java8.Consumer;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;

public class MarkLogicDatasetRuntimeTest {

    private MarkLogicDatasetRuntime datasetRuntime;

    @Before
    public void setup() {
        datasetRuntime = new MarkLogicDatasetRuntime();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWorkflow() {

        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        MarkLogicConnectionProperties connection = new MarkLogicConnectionProperties("datastore");
        connection.referencedComponent.componentInstanceId.setValue("reference");
        DatabaseClient connectionClient = Mockito.mock(DatabaseClient.class);
        GenericDocumentManager docManager = Mockito.mock(GenericDocumentManager.class);
        Mockito.when(connectionClient.newDocumentManager()).thenReturn(docManager);
        QueryManager queryManager = Mockito.mock(QueryManager.class);
        Mockito.when(connectionClient.newQueryManager()).thenReturn(queryManager);
        StringQueryDefinition stringQueryDefinition = Mockito.mock(StringQueryDefinition.class);
        Mockito.when(queryManager.newStringDefinition()).thenReturn(stringQueryDefinition);
        SearchHandle searchHandle = Mockito.mock(SearchHandle.class);
        Mockito.when(queryManager.search(Mockito.eq(stringQueryDefinition), Mockito.any(SearchHandle.class)))
                .thenReturn(searchHandle);

        MatchDocumentSummary[] results = new MatchDocumentSummary[2];
        MatchDocumentSummary firstRecord = Mockito.mock(MatchDocumentSummary.class);
        MatchDocumentSummary secondRecord = Mockito.mock(MatchDocumentSummary.class);
        results[0] = firstRecord;
        results[1] = secondRecord;

        Mockito.when(container.getComponentData("reference", MarkLogicConnection.CONNECTION)).thenReturn(connectionClient);
        MarkLogicDatasetProperties properties = new MarkLogicDatasetProperties("dataset");
        connection.init();
        properties.setDatastoreProperties(connection);
        properties.init();
        datasetRuntime.initialize(container, properties);

        Assert.assertEquals(2, datasetRuntime.getSchema().getFields().size());
        Assert.assertEquals("docId", datasetRuntime.getSchema().getFields().get(0).name());
        Assert.assertEquals("docContent", datasetRuntime.getSchema().getFields().get(1).name());
        Mockito.when(searchHandle.getMatchResults()).thenReturn(results);
        Mockito.when(searchHandle.getTotalResults()).thenReturn(2l);

        Mockito.when(docManager.readAs(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(new Object());

        Consumer<IndexedRecord> consumer = Mockito.mock(Consumer.class);
        datasetRuntime.getSample(2, consumer);

        Mockito.verify(consumer, Mockito.times(2)).accept(Mockito.any(IndexedRecord.class));
    }
}
