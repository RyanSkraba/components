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

package org.talend.components.salesforce.runtime.common;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult;

import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.IDescribeGlobalSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

/**
 *
 */
public class SalesforceRuntimeCommonTest {

    @Test
    public void testExceptionToValidationResult() throws Exception {

        ValidationResult vr1 = SalesforceRuntimeCommon.exceptionToValidationResult(
                new RuntimeException("TEST ERROR MESSAGE"));
        assertEquals(ValidationResult.Result.ERROR, vr1.getStatus());
        assertThat(vr1.getMessage(), containsString("TEST ERROR MESSAGE"));

        ValidationResult vr2 = SalesforceRuntimeCommon.exceptionToValidationResult(
                new RuntimeException(""));
        assertEquals(ValidationResult.Result.ERROR, vr2.getStatus());
        assertThat(vr2.getMessage(), containsString("RuntimeException"));
    }

    @Test
    public void testGetSchemaNames() throws Exception {

        DescribeGlobalResult result = new DescribeGlobalResult();

        DescribeGlobalSObjectResult sObjectResult1 = new DescribeGlobalSObjectResult();
        sObjectResult1.setName("Account");
        sObjectResult1.setLabel("Account");

        DescribeGlobalSObjectResult sObjectResult2 = new DescribeGlobalSObjectResult();
        sObjectResult2.setName("Contact");
        sObjectResult2.setLabel("Contact");

        DescribeGlobalSObjectResult sObjectResult3 = new DescribeGlobalSObjectResult();
        sObjectResult3.setName("Campaign");
        sObjectResult3.setLabel("Campaign");

        result.setSobjects(new IDescribeGlobalSObjectResult[]{
                sObjectResult1, sObjectResult2, sObjectResult3
        });

        PartnerConnection conn = mock(PartnerConnection.class);
        doReturn(result).when(conn).describeGlobal();

        List<NamedThing> schemaNames = SalesforceRuntimeCommon.getSchemaNames(conn);

        assertThat(schemaNames, containsInAnyOrder(
                (NamedThing) new SimpleNamedThing("Account", "Account"),
                new SimpleNamedThing("Contact", "Contact"),
                new SimpleNamedThing("Campaign", "Campaign")
        ));
    }

    @Test(expected = ComponentException.class)
    public void testGetSchemaNamesError() throws Exception {

        PartnerConnection conn = mock(PartnerConnection.class);
        doThrow(new ConnectionException("CONNECTION ERROR")).when(conn).describeGlobal();

        SalesforceRuntimeCommon.getSchemaNames(conn);
    }

    @Test
    public void testRenewSession() throws Exception {

        SessionRenewer sessionRenewer = mock(SessionRenewer.class);

        ConnectorConfig connectorConfig = new ConnectorConfig();
        connectorConfig.setSessionRenewer(sessionRenewer);

        SalesforceRuntimeCommon.renewSession(connectorConfig);

        verify(sessionRenewer, only()).renewSession(eq(connectorConfig));
    }
}
