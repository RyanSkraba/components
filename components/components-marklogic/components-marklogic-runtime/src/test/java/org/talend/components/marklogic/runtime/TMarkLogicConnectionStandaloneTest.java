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
package org.talend.components.marklogic.runtime;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.MarkLogicInternalException;
import com.marklogic.client.Transaction;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseClientFactory.class)
public class TMarkLogicConnectionStandaloneTest {

    private TMarkLogicConnectionStandalone connectionStandalone;

    private RuntimeContainer container;

    private MarkLogicConnectionProperties connectionProperties;

    private DatabaseClient client;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Before
    public void setup() {
        connectionStandalone = new TMarkLogicConnectionStandalone();

        PowerMockito.mockStatic(DatabaseClientFactory.class);
        client = Mockito.mock(DatabaseClient.class);
        Mockito.when(DatabaseClientFactory.newClient(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.any(SecurityContext.class))).thenReturn(client);
        container = Mockito.mock(RuntimeContainer.class);
        connectionProperties = new MarkLogicConnectionProperties("connection");
        connectionProperties.authentication.setValue("BASIC");
        Mockito.when(container.getComponentData(Mockito.anyString(), Mockito.eq(MarkLogicConnection.CONNECTION)))
                .thenReturn(client);

    }

    @Test
    public void testConnectSuccess() {
        Transaction transaction = Mockito.mock(Transaction.class);
        Mockito.when(client.openTransaction()).thenReturn(transaction);

        Mockito.when(container.getCurrentComponentId()).thenReturn("connectionComponent");
        connectionStandalone.initialize(container, connectionProperties);

        connectionStandalone.runAtDriver(container);

        Mockito.verify(transaction, Mockito.only()).commit();
        Mockito.verify(container).setComponentData("connectionComponent", MarkLogicConnection.CONNECTION, client);
    }

    @Test
    public void testConnectFailureInContainer() {
        exceptions.expect(ComponentException.class);
        exceptions.expectMessage(Matchers.containsString("Referenced component: "));
        exceptions.expectMessage(Matchers.containsString("not connected"));

        connectionProperties.referencedComponent.componentInstanceId.setValue("ref1");
        connectionStandalone.initialize(container, connectionProperties);
        Mockito.when(container.getCurrentComponentId()).thenReturn("tMarkLogicConnection1");
        Mockito.when(container.getComponentData(Mockito.anyString(), Mockito.eq(MarkLogicConnection.CONNECTION)))
                .thenReturn(null);

        connectionStandalone.runAtDriver(container);

    }

    @Test
    public void testConnectGetConnectionFromContainer() {

        connectionProperties.referencedComponent.componentInstanceId.setValue("ref1");
        connectionStandalone.initialize(container, connectionProperties);
        Mockito.when(container.getCurrentComponentId()).thenReturn("tMarkLogicConnection1");

        connectionStandalone.runAtDriver(container);

        Mockito.verify(container).getComponentData(Mockito.anyString(), Mockito.eq(MarkLogicConnection.CONNECTION));
    }

    @Test
    public void testConnectionFailureToConnectInvalidCredentials() {
        exceptions.expect(MarkLogicException.class);
        exceptions.expectMessage(Matchers.containsString("Invalid credentials."));

        connectionProperties.authentication.setValue("DIGEST");
        connectionStandalone.initialize(container, connectionProperties);
        Mockito.when(client.openTransaction()).thenThrow(new FailedRequestException("Wrong password"));

        connectionStandalone.runAtDriver(container);
    }

    @Test
    public void testConnectionFailureToConnectServerIsDown() {
        exceptions.expect(MarkLogicException.class);
        exceptions.expectMessage(
                Matchers.containsString("Cannot connect to MarkLogic database. Check your database connectivity."));

        connectionStandalone.initialize(container, connectionProperties);
        Mockito.when(client.openTransaction())
                .thenThrow(new MarkLogicInternalException("transaction open produced invalid location"));

        connectionStandalone.runAtDriver(container);
    }
}
