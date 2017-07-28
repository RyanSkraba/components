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
package org.talend.components.marketo.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoSinkTest {

    MarketoSink sink;

    @Before
    public void setUp() throws Exception {
        sink = new MarketoSink();
    }

    @Test
    public void testValidate() throws Exception {
        //
        // output
        //
        TMarketoOutputProperties oprops = new TMarketoOutputProperties("test");
        oprops.connection.setupProperties();
        oprops.connection.endpoint.setValue("http://ABC.mkto.com");
        oprops.connection.clientAccessId.setValue("fakeId");
        oprops.connection.secretKey.setValue("sekret");
        oprops.setupProperties();

        MarketoSink spy = spy(sink);
        spy.initialize(null, oprops);
        doReturn(oprops.connection).when(spy).getEffectiveConnection(any(RuntimeContainer.class));
        doReturn(new ValidationResult(Result.ERROR)).when(spy).validateConnection(any(MarketoProvideConnectionProperties.class));

        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        doReturn(ValidationResult.OK).when(spy).validateConnection(any(MarketoProvideConnectionProperties.class));
        //
        oprops.outputOperation.setValue(OutputOperation.syncMultipleLeads);
        spy.initialize(null, oprops);
        assertEquals(Result.OK, spy.validate(null).getStatus());
        //
        oprops.outputOperation.setValue(OutputOperation.deleteLeads);
        spy.initialize(null, oprops);
        assertEquals(Result.OK, spy.validate(null).getStatus());
        //
        oprops.outputOperation.setValue(OutputOperation.deleteCustomObjects);
        spy.initialize(null, oprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        oprops.outputOperation.setValue(OutputOperation.syncCustomObjects);
        spy.initialize(null, oprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        oprops.outputOperation.setValue(OutputOperation.deleteCustomObjects);
        oprops.customObjectName.setValue("co");
        spy.initialize(null, oprops);
        assertEquals(Result.OK, spy.validate(null).getStatus());
        //
        oprops.outputOperation.setValue(OutputOperation.syncCustomObjects);
        oprops.customObjectName.setValue("co");
        spy.initialize(null, oprops);
        assertEquals(Result.OK, spy.validate(null).getStatus());
        //
        // input
        //
        TMarketoInputProperties iprops = new TMarketoInputProperties("test");
        iprops.connection.setupProperties();
        iprops.connection.endpoint.setValue("http://ABC.mkto.com");
        iprops.connection.clientAccessId.setValue("fakeId");
        iprops.connection.secretKey.setValue("sekret");
        iprops.setupProperties();

        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.inputOperation.setValue(InputOperation.getMultipleLeads);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.connection.apiMode.setValue(APIMode.SOAP);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.leadSelectorSOAP.setValue(LeadSelector.StaticListSelector);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.leadSelectorSOAP.setValue(LeadSelector.LeadKeySelector);
        iprops.leadKeyValues.setValue("test");
        spy.initialize(null, iprops);
        assertEquals(Result.OK, spy.validate(null).getStatus());
    }

    @Test
    public void testCreateWriteOperation() throws Exception {
        assertNotNull(new MarketoSink().createWriteOperation());
        sink.properties = new TMarketoListOperationProperties("test");
        assertTrue(sink.createWriteOperation() instanceof MarketoWriteOperation);
        sink.properties = new TMarketoOutputProperties("test");
        assertTrue(sink.createWriteOperation() instanceof MarketoWriteOperation);
    }

}
