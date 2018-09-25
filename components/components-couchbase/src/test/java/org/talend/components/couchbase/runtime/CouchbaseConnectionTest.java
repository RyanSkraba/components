package org.talend.components.couchbase.runtime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.env.CouchbaseEnvironment;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CouchbaseCluster.class)
public class CouchbaseConnectionTest {

    private CouchbaseConnection connection;
    private CouchbaseCluster cluster;
    private Bucket bucket;

    @Before
    public void setup() {
        PowerMockito.mockStatic(CouchbaseCluster.class);
        cluster = Mockito.mock(CouchbaseCluster.class);
        Mockito.when(CouchbaseCluster.create(Mockito.any(CouchbaseEnvironment.class), Mockito.eq("testNode"))).thenReturn(cluster);
        connection = new CouchbaseConnection("testNode", "testBucket", "defaultPassword");
        bucket = Mockito.mock(Bucket.class);
        Mockito.when(cluster.openBucket(Mockito.anyString(), Mockito.anyString())).thenReturn(bucket);
    }

    @Test
    public void testConnect() {

        connection.connect();

        Mockito.verify(cluster).openBucket(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUpsert() {

        connection.connect();

        Mockito.when(bucket.upsert(Mockito.any(RawJsonDocument.class))).thenReturn(Mockito.mock(RawJsonDocument.class));
        connection.upsert("someId", "content");

        Mockito.verify(bucket).upsert(Mockito.any(RawJsonDocument.class));
    }

    @Test
    public void testIncrementAndDecrement() {
        connection.connect();

        // Increment and decrement to call bucket close.
        connection.increment();
        connection.decrement();

        Mockito.verify(bucket).close();
    }
}
