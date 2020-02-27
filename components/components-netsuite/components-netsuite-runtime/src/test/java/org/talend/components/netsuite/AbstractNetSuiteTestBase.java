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

import static org.talend.components.netsuite.client.model.beans.Beans.getProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.Schema;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.test.TestFixture;
import org.talend.components.netsuite.client.EmptyCustomMetaDataSource;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.client.model.beans.PropertyInfo;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.components.netsuite.test.TestUtils;
import org.talend.daikon.avro.AvroUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 */
public abstract class AbstractNetSuiteTestBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static TestFixtures classScopedTestFixtures = new TestFixtures();

    protected TestFixtures testFixtures = new TestFixtures();

    protected static final Random rnd = new Random(System.currentTimeMillis());

    protected static final DatatypeFactory datatypeFactory;

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected static final String CONNECTION_COMPONENT_ID = "tNetsuiteTestConnection";

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setUp() throws Exception {
        testFixtures.setUp();
    }

    public void tearDown() throws Exception {
        testFixtures.tearDown();
    }

    public static void setUpClassScopedTestFixtures() throws Exception {
        classScopedTestFixtures.setUp();
    }

    public static void tearDownClassScopedTestFixtures() throws Exception {
        classScopedTestFixtures.tearDown();
    }

    protected Schema getDynamicSchema() {
        Schema emptySchema = Schema.createRecord("dynamic", null, null, false);
        emptySchema.setFields(new ArrayList<Schema.Field>());
        emptySchema = AvroUtils.setIncludeAllFields(emptySchema, true);
        return emptySchema;
    }

    protected static Schema.Field getFieldByName(List<Schema.Field> fields, String name) {
        for (Schema.Field field : fields) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        return null;
    }

    protected static <T> List<T> makeNsObjects(ObjectComposer<T> composer, int count) throws Exception {
        List<T> recordList = new ArrayList<>();
        while (count > 0) {
            T record = composer.composeObject();
            recordList.add(record);
            count--;
        }
        return recordList;
    }

    protected static <T> T composeObject(Class<T> clazz) throws Exception {
        BeanInfo beanInfo = Beans.getBeanInfo(clazz);
        List<PropertyInfo> propertyInfoList = beanInfo.getProperties();

        T obj = clazz.newInstance();

        for (PropertyInfo propertyInfo : propertyInfoList) {
            if (propertyInfo.getWriteType() != null) {
                Object value;
                if (propertyInfo.getName().equals("internalId")) {
                    value = composeValue(Integer.class).toString();
                } else {
                    value = composeValue(propertyInfo.getWriteType());
                }
                setProperty(obj, propertyInfo.getName(), value);
            }
        }

        return obj;
    }

    protected static Object composeValue(Class<?> clazz) throws Exception {
        if (clazz == Boolean.class) {
            return Boolean.valueOf(rnd.nextBoolean());
        }
        if (clazz == Long.class) {
            return Long.valueOf(rnd.nextLong());
        }
        if (clazz == Double.class) {
            return Double.valueOf(rnd.nextLong());
        }
        if (clazz == Integer.class) {
            return Integer.valueOf(rnd.nextInt());
        }
        if (clazz == String.class) {
            int len = 10 + rnd.nextInt(100);
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                sb.append((char) (32 + rnd.nextInt(127 - 32)));
            }
            return sb.toString();
        }
        if (clazz == XMLGregorianCalendar.class) {
            return composeDateTime();
        }
        if (clazz.isEnum()) {
            Object[] values = clazz.getEnumConstants();
            return values[rnd.nextInt(values.length)];
        }
        return null;
    }

    protected static XMLGregorianCalendar composeDateTime() throws Exception {
        DateTime dateTime = DateTime.now();

        XMLGregorianCalendar xts = datatypeFactory.newXMLGregorianCalendar();
        xts.setYear(dateTime.getYear());
        xts.setMonth(dateTime.getMonthOfYear());
        xts.setDay(dateTime.getDayOfMonth());
        xts.setHour(dateTime.getHourOfDay());
        xts.setMinute(dateTime.getMinuteOfHour());
        xts.setSecond(dateTime.getSecondOfMinute());
        xts.setMillisecond(dateTime.getMillisOfSecond());
        xts.setTimezone(dateTime.getZone().toTimeZone().getRawOffset() / 60000);

        return xts;

    }

    public interface ObjectComposer<T> {

        T composeObject() throws Exception;
    }

    public static class SimpleObjectComposer<T> implements ObjectComposer<T> {
        protected Class<T> clazz;

        public SimpleObjectComposer(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T composeObject() throws Exception {
            return AbstractNetSuiteTestBase.composeObject(clazz);
        }
    }

    public static class RecordRefComposer<T> implements ObjectComposer<T> {
        protected Class<T> clazz;

        public RecordRefComposer(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T composeObject() throws Exception {
            T nsObject = AbstractNetSuiteTestBase.composeObject(clazz);
            setSimpleProperty(nsObject, "type", null);
            return nsObject;
        }
    }

    public static class NsObjectComposer<T> implements ObjectComposer<T> {
        protected MetaDataSource metaDataSource;
        protected TypeDesc typeDesc;

        public NsObjectComposer(MetaDataSource metaDataSource, TypeDesc typeDesc) {
            this.metaDataSource = metaDataSource;
            this.typeDesc = typeDesc;
        }

        @Override
        public T composeObject() throws Exception {
            T record = (T) AbstractNetSuiteTestBase.composeObject(typeDesc.getTypeClass());

            List<Object> customFields = new ArrayList<>();
            for (FieldDesc fieldDesc : typeDesc.getFields()) {
                if (fieldDesc instanceof CustomFieldDesc) {
                    CustomFieldDesc customFieldDesc = fieldDesc.asCustom();
                    Object customField = metaDataSource.getBasicMetaData().createInstance(
                            customFieldDesc.getCustomFieldType().getTypeName());

                    Beans.setProperty(customField, "scriptId",
                            customFieldDesc.getCustomizationRef().getScriptId());
                    Beans.setProperty(customField, "internalId",
                            customFieldDesc.getCustomizationRef().getInternalId());

                    BeanInfo beanInfo = Beans.getBeanInfo(customField.getClass());
                    PropertyInfo valuePropInfo = beanInfo.getProperty("value");

                    Object value = composeValue(valuePropInfo.getWriteType());
                    if (value != null) {
                        Beans.setProperty(customField, "value", value);
                    }

                    customFields.add(customField);
                }
            }

            if (!customFields.isEmpty()) {
                Beans.setProperty(record, "customFieldList",
                        metaDataSource.getBasicMetaData().createInstance("CustomFieldList"));
                List<Object> customFieldList = (List<Object>) getProperty(record, "customFieldList.customField");
                customFieldList.addAll(customFields);
            }

            return record;
        }
    }

    public static class TestFixtures implements TestFixture {
        protected final Logger logger = LoggerFactory.getLogger(getClass());

        protected List<TestFixture> testFixtures = new ArrayList<>();

        public void add(TestFixture testFixture) {
            testFixtures.add(testFixture);
        }

        public void clear() {
            testFixtures.clear();
        }

        @Override
        public void setUp() throws Exception {
            for (TestFixture testFixture : testFixtures) {
                testFixture.setUp();
            }
        }

        @Override
        public void tearDown() throws Exception {
            List<TestFixture> reversed = new ArrayList<>(testFixtures);
            Collections.reverse(reversed);
            for (TestFixture testFixture : reversed) {
                try {
                    testFixture.tearDown();
                } catch (Exception | Error e) {
                    logger.error("Failed to tearDown test fixture: {}", testFixture, e);
                }
            }
            clear();
        }
    }

    public static class TestCustomMetaDataSource extends EmptyCustomMetaDataSource {

        protected static final ObjectMapper objectMapper = new ObjectMapper();

        protected NetSuiteClientService<?> clientService;

        protected List<String> typeNames;

        public TestCustomMetaDataSource(NetSuiteClientService<?> clientService) {
            this(clientService, Collections.<String>emptyList());
        }

        public TestCustomMetaDataSource(NetSuiteClientService<?> clientService, String typeName) {
            this(clientService, Collections.singletonList(typeName));
        }

        public TestCustomMetaDataSource(NetSuiteClientService<?> clientService, List<String> typeNames) {
            this.clientService = clientService;
            this.typeNames = typeNames;
        }

        @Override
        public Collection<RecordTypeInfo> getCustomRecordTypes() {
            return Arrays.asList(getCustomRecordType("custom_record_type_1"));
        }

        @Override
        public RecordTypeInfo getCustomRecordType(String typeName) {
            try {
                if (typeName.equals("custom_record_type_1")) {
                    JsonNode recordTypeNode = objectMapper.readTree(getClass().getResource(
                            "/test-data/customRecord-1.json"));
                    RecordTypeInfo customRecordTypeInfo =
                            TestUtils.readCustomRecord(clientService.getBasicMetaData(), recordTypeNode);
                    return customRecordTypeInfo;
                }
                return null;
            } catch (IOException e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

        @Override
        public Map<String, CustomFieldDesc> getCustomFields(RecordTypeInfo recordTypeInfo) {
            try {
                if (recordTypeInfo.getName().equals("custom_record_type_1") ||
                        typeNames.contains(recordTypeInfo.getName())) {
                    JsonNode fieldListNode = objectMapper.readTree(getClass().getResource(
                            "/test-data/customRecordFields-1.json"));
                    Map<String, CustomFieldDesc> customFieldDescMap =
                            TestUtils.readCustomFields(fieldListNode);
                    return customFieldDescMap;
                }
                return Collections.emptyMap();
            } catch (IOException e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

    }

    protected static NetSuiteConnectionProperties getConnectionProperties() {
        NetSuiteConnectionProperties connectionProperties = new NetSuiteConnectionProperties("connection");
        NetSuiteWebServiceTestFixture webServiceTestFixture = (NetSuiteWebServiceTestFixture) classScopedTestFixtures.testFixtures.get(0);
        connectionProperties.init();
        connectionProperties.endpoint.setValue(webServiceTestFixture.getEndpointUrl());
        connectionProperties.email.setValue(webServiceTestFixture.getCredentials().getEmail());
        connectionProperties.password.setValue(webServiceTestFixture.getCredentials().getPassword());
        connectionProperties.account.setValue(webServiceTestFixture.getCredentials().getAccount());
        connectionProperties.role.setValue(Integer.valueOf(webServiceTestFixture.getCredentials().getRoleId()));
        connectionProperties.applicationId.setValue(webServiceTestFixture.getCredentials().getApplicationId());
        return connectionProperties;
    }

    protected static RuntimeContainer getRuntimeContainer() {
        return new RuntimeContainer() {

            private final Map<String, Object> globalMap = new HashMap<>();

            @Override
            public void setComponentData(String componentId, String key, Object data) {
                globalMap.put(componentId + key, data);
            }

            @Override
            public Object getGlobalData(String key) {
                return globalMap.get(key);
            }

            @Override
            public String getCurrentComponentId() {
                return CONNECTION_COMPONENT_ID;
            }

            @Override
            public Object getComponentData(String componentId, String key) {
                return globalMap.get(componentId + key);
            }
        };
    }
}
