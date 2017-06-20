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

package org.talend.components.netsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl.getNsFieldName;
import static org.talend.components.netsuite.client.NetSuiteClientService.MESSAGE_LOGGING_ENABLED_PROPERTY_NAME;
import static org.talend.components.netsuite.client.model.beans.Beans.getProperty;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.cxf.headers.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.test.TestFixture;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteCredentials;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.test.FreePortFinder;
import org.talend.components.netsuite.util.Mapper;

/**
 *
 */
public class NetSuiteWebServiceMockTestFixture<PortT, AdapterT extends NetSuitePortTypeMockAdapter<PortT>>
        implements TestFixture {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private NetSuiteClientFactory<PortT> clientFactory;
    private NetSuiteServiceFactory<?> serviceFactory;
    private Class<PortT> portClass;
    private Class<AdapterT> portAdapterClass;
    private String portName;

    private Endpoint endpoint;
    private Object service;
    private NetSuiteCredentials credentials;
    private AdapterT portMockAdapter;
    private PortT portMock;
    private NetSuiteClientService<PortT> clientService;

    public NetSuiteWebServiceMockTestFixture(
            NetSuiteServiceFactory serviceFactory,
            NetSuiteClientFactory<PortT> clientFactory,
            Class<PortT> portClass,
            Class<AdapterT> portAdapterClass,
            String portName) {

        this.serviceFactory = serviceFactory;
        this.clientFactory = clientFactory;
        this.portClass = portClass;
        this.portAdapterClass = portAdapterClass;
        this.portName = portName;
    }

    @Override
    public void setUp() throws Exception {
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        int retryCount = 3;
        while (retryCount > 0) {
            retryCount--;
            try {
                publish();
                break;
            } catch (WebServiceException | IOException e) {
                logger.error("Service publishing error: {}", e);
                if (retryCount == 0) {
                    throw e;
                }
            }
        }

        URL wsdlLocation = new URL(portMockAdapter.getEndpointAddress().toString().concat("?wsdl"));
//        assertNotNull(wsdlLocation.getContent());

        service = serviceFactory.createService(wsdlLocation);

        credentials = new NetSuiteCredentials(
                "test@test.com", "12345", "test", "3");
        credentials.setApplicationId("00000000-0000-0000-0000-000000000000");

        reinstall();
    }

    protected void publish() throws Exception {
        int portNumber = FreePortFinder.findFreePort(28080, FreePortFinder.MAX_PORT_NUMBER);
        URL endpointAddress = new URL("http://localhost:" + portNumber + "/services/" + portName);

        logger.info("Endpoint address: {}", endpointAddress);

        portMockAdapter = portAdapterClass.newInstance();
        portMockAdapter.setEndpointAddress(endpointAddress);

        // Publish the SOAP Web Service
        endpoint = Endpoint.publish(endpointAddress.toString(), portMockAdapter);
        assertTrue(endpoint.isPublished());
        assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", endpoint.getBinding().getBindingID());
    }

    @Override
    public void tearDown() throws Exception {
        // Unpublish the SOAP Web Service
        if (endpoint != null) {
            endpoint.stop();
            assertFalse(endpoint.isPublished());
        }

        service = null;
        portMockAdapter = null;
    }

    public PortT createPortMock() {
        return mock(portClass);
    }

    public void reinstall() {
        portMock = createPortMock();
        portMockAdapter.setPort(portMock);

        clientService = clientFactory.createClient();
        clientService.setEndpointUrl(getEndpointAddress().toString());
        clientService.setCredentials(credentials);

        boolean messageLoggingEnabled = Boolean.valueOf(
                System.getProperty(MESSAGE_LOGGING_ENABLED_PROPERTY_NAME, "false"));
        clientService.setMessageLoggingEnabled(messageLoggingEnabled);
    }

    public NetSuiteClientFactory<PortT> getClientFactory() {
        return clientFactory;
    }

    public NetSuiteCredentials getCredentials() {
        return credentials;
    }

    public AdapterT getPortMockAdapter() {
        return portMockAdapter;
    }

    public PortT getPortMock() {
        return portMock;
    }

    public URL getEndpointAddress()  {
        return portMockAdapter.getEndpointAddress();
    }

    public NetSuiteClientService<PortT> getClientService() {
        return clientService;
    }

    public static Header getHeader(List<Header> headers, QName name) {
        for (Header header : headers) {
            if (name.equals(header.getName())) {
                return header;
            }
        }
        return null;
    }

    public static void assertIndexedRecord(TypeDesc typeDesc, IndexedRecord indexedRecord) throws Exception {
        assertNotNull(indexedRecord);

        Schema recordSchema = indexedRecord.getSchema();
        assertEquals(typeDesc.getFields().size(), recordSchema.getFields().size());

        for (Schema.Field field : recordSchema.getFields()) {
            String nsFieldName = getNsFieldName(field);
            FieldDesc fieldDesc = typeDesc.getField(nsFieldName);
            assertNotNull(field);

            Object value = indexedRecord.get(field.pos());

            if (fieldDesc instanceof CustomFieldDesc) {
                CustomFieldDesc customFieldDesc = fieldDesc.asCustom();
                switch (customFieldDesc.getCustomFieldType()) {
                case BOOLEAN:
                case LONG:
                case DOUBLE:
                case STRING:
                case DATE:
                    assertNotNull(value);
                    break;
                }
            } else {
                Class<?> datumClass = fieldDesc.getValueType();

                if (datumClass == Boolean.class ||
                        datumClass == Long.class ||
                        datumClass == Double.class ||
                        datumClass == String.class ||
                        datumClass == XMLGregorianCalendar.class) {
                    assertNotNull(value);
                } else if (datumClass.isEnum()) {
                    assertNotNull(value);
                    Mapper<String, Enum> enumAccessor = Beans.getEnumFromStringMapper((Class<Enum>) datumClass);
                    Enum modelValue = enumAccessor.map((String) value);
                    assertNotNull(modelValue);
                }
            }
        }
    }

    public static void assertNsObject(TypeDesc typeDesc, Object nsObject) throws Exception {
        assertNotNull(nsObject);
        assertEquals(typeDesc.getTypeClass(), nsObject.getClass());

        for (FieldDesc fieldDesc : typeDesc.getFields()) {
            String fieldName = fieldDesc.getName();

            Object value = getProperty(nsObject, fieldName);

            if (fieldDesc instanceof CustomFieldDesc) {
                CustomFieldDesc customFieldDesc = fieldDesc.asCustom();
                switch (customFieldDesc.getCustomFieldType()) {
                case BOOLEAN:
                case LONG:
                case DOUBLE:
                case STRING:
                case DATE:
                    assertNotNull(value);
                    break;
                }
            } else {
                Class<?> datumClass = fieldDesc.getValueType();

                if (datumClass == Boolean.class ||
                        datumClass == Long.class ||
                        datumClass == Double.class ||
                        datumClass == String.class ||
                        datumClass == XMLGregorianCalendar.class) {
                    assertNotNull(value);
                } else if (datumClass.isEnum()) {
                    assertNotNull(value);
                }
            }
        }
    }

    public interface NetSuiteServiceFactory<T> {

        T createService(URL endpointUrl);
    }
}
