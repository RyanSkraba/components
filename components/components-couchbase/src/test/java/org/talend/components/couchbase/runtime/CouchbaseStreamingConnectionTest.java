package org.talend.components.couchbase.runtime;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.couchbase.client.dcp.Client;
import com.couchbase.client.dcp.Client.Builder;
import com.couchbase.client.dcp.StreamFrom;
import com.couchbase.client.dcp.StreamTo;
import com.couchbase.client.dcp.config.DcpControl.Names;
import com.couchbase.client.dcp.state.SessionState;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

import rx.Completable;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Client.class)
public class CouchbaseStreamingConnectionTest {

    private CouchbaseStreamingConnection streamingConnection;

    private Client client;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Client.class);
        Builder builder = Mockito.mock(Builder.class);
        Mockito.when(builder.connectTimeout(Mockito.anyLong())).thenReturn(builder);
        Mockito.when(builder.hostnames(Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.bucket(Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.password(Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.controlParam(Mockito.any(Names.class), Mockito.any())).thenReturn(builder);
        Mockito.when(builder.bufferAckWatermark(Mockito.anyInt())).thenReturn(builder);
        client = Mockito.mock(Client.class);
        PowerMockito.when(Client.configure()).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(client);
        streamingConnection = new CouchbaseStreamingConnection("localhost", "", "testPassword");
    }

    @Test
    public void testConnect() {
        Completable completable = Completable.complete();
        Mockito.when(client.connect()).thenReturn(completable);
        streamingConnection.connect();
        Assert.assertTrue(!streamingConnection.isClosed());
    }

    @Test
    public void testDisconnect() {
        Completable completable = Completable.complete();
        Mockito.when(client.connect()).thenReturn(completable);
        Mockito.when(client.disconnect()).thenReturn(completable);
        streamingConnection.connect();

        streamingConnection.disconnect();
        Assert.assertTrue(streamingConnection.isClosed());
    }

    @Test
    public void testStartStreaming() throws InterruptedException {
        Mockito.when(client.initializeState(StreamFrom.BEGINNING, StreamTo.NOW)).thenReturn(Completable.complete());
        Mockito.when(client.startStreaming(Mockito.<Short[]>anyVararg())).thenReturn(Completable.complete());
        SessionState sessionState = Mockito.mock(SessionState.class);
        Mockito.when(sessionState.isAtEnd()).thenReturn(false, false, true);
        Mockito.when(client.sessionState()).thenReturn(sessionState);

        BlockingQueue<ByteBuf> resultsQueue = new ArrayBlockingQueue<>(3);

        streamingConnection.startStreaming(resultsQueue);

        Assert.assertTrue(streamingConnection.isStreaming());

        Thread.sleep(2000);
        Mockito.verify(client, Mockito.times(3)).sessionState();
    }

    @Test
    public void testStartStopStreaming() throws InterruptedException {
        Mockito.when(client.initializeState(StreamFrom.BEGINNING, StreamTo.NOW)).thenReturn(Completable.complete());
        Mockito.when(client.startStreaming(Mockito.<Short[]>anyVararg())).thenReturn(Completable.complete());
        Mockito.when(client.stopStreaming(Mockito.<Short[]>anyVararg())).thenReturn(Completable.complete());
        SessionState sessionState = Mockito.mock(SessionState.class);
        Mockito.when(sessionState.isAtEnd()).thenReturn(false, true);
        Mockito.when(client.sessionState()).thenReturn(sessionState);

        BlockingQueue<ByteBuf> resultsQueue = new ArrayBlockingQueue<>(4);
        resultsQueue.put(Mockito.mock(ByteBuf.class));
        resultsQueue.put(Mockito.mock(ByteBuf.class));
        resultsQueue.put(Mockito.mock(ByteBuf.class));
        resultsQueue.put(Mockito.mock(ByteBuf.class));
        Mockito.when(client.disconnect()).thenReturn(Completable.complete());


        streamingConnection.startStreaming(resultsQueue);
        streamingConnection.stopStreaming();

        Thread.sleep(1500);
        Mockito.verify(client, Mockito.times(2)).sessionState();
        Mockito.verify(client, Mockito.times(1)).disconnect();
    }

}
