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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.Schema;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.input.NsObjectInputTransducer;
import org.talend.components.netsuite.json.NsTypeResolverBuilder;
import org.talend.components.netsuite.v2016_2.client.NetSuiteClientServiceImpl;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.exception.ExceptionContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netsuite.webservices.v2016_2.lists.accounting.Account;
import com.netsuite.webservices.v2016_2.lists.accounting.types.AccountType;
import com.netsuite.webservices.v2016_2.lists.accounting.types.ConsolidatedRate;
import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.core.BooleanCustomFieldRef;
import com.netsuite.webservices.v2016_2.platform.core.CustomFieldList;
import com.netsuite.webservices.v2016_2.platform.core.RecordRef;
import com.netsuite.webservices.v2016_2.platform.core.RecordRefList;
import com.netsuite.webservices.v2016_2.platform.core.StringCustomFieldRef;

/**
 *
 */
public class ValueConverterTest extends NetSuiteMockTestBase {

    private NetSuiteClientService<NetSuitePortType> clientService = new NetSuiteClientServiceImpl();
    private TypeDesc typeDesc;
    private Schema schema;

    @Before
    public void setUp() throws Exception {
        typeDesc = clientService.getBasicMetaData().getTypeInfo("Account");

        schema = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());
    }

    @Test
    public void testIdentityConverters() throws Exception {
        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        FieldDesc fieldDesc = typeDesc.getField("isInactive");
        AvroConverter<Boolean, Boolean> converter1 =
                (AvroConverter<Boolean, Boolean>) transducer.getValueConverter(fieldDesc);
        assertEquals(Boolean.TRUE, converter1.convertToAvro(Boolean.TRUE));
        assertEquals(Boolean.FALSE, converter1.convertToDatum(Boolean.FALSE));

        fieldDesc = typeDesc.getField("openingBalance");
        AvroConverter<Double, Double> converter2 =
                (AvroConverter<Double, Double>) transducer.getValueConverter(fieldDesc);
        assertEquals(Double.valueOf(12345.6789), converter2.convertToAvro(Double.valueOf(12345.6789)));
        assertEquals(Double.valueOf(98765.4321), converter2.convertToDatum(Double.valueOf(98765.4321)));
    }

    @Test
    public void testEnumConverter() throws Exception {
        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        FieldDesc fieldDesc = typeDesc.getField("acctType");
        AvroConverter<Enum<AccountType>, String> converter1 =
                (AvroConverter<Enum<AccountType>, String>) transducer.getValueConverter(fieldDesc);
        assertEquals(AccountType.ACCOUNTS_PAYABLE.value(),
                converter1.convertToAvro(AccountType.ACCOUNTS_PAYABLE));
        assertEquals(AccountType.ACCOUNTS_PAYABLE,
                converter1.convertToDatum(AccountType.ACCOUNTS_PAYABLE.value()));

        fieldDesc = typeDesc.getField("generalRate");
        assertNotNull(fieldDesc);
        AvroConverter<Enum<ConsolidatedRate>, String> converter2 =
                (AvroConverter<Enum<ConsolidatedRate>, String>) transducer.getValueConverter(fieldDesc);
        assertEquals(ConsolidatedRate.HISTORICAL.value(),
                converter2.convertToAvro(ConsolidatedRate.HISTORICAL));
        assertEquals(ConsolidatedRate.HISTORICAL,
                converter2.convertToDatum(ConsolidatedRate.HISTORICAL.value()));
    }

    @Test
    public void testXMLGregorianCalendarConverter() throws Exception {
        DateTimeZone tz1 = DateTimeZone.getDefault();

        MutableDateTime dateTime1 = new MutableDateTime(tz1);
        dateTime1.setDate(System.currentTimeMillis());
        Long controlValue1 = dateTime1.getMillis();

        XMLGregorianCalendar xmlCalendar1 = datatypeFactory.newXMLGregorianCalendar();
        xmlCalendar1.setYear(dateTime1.getYear());
        xmlCalendar1.setMonth(dateTime1.getMonthOfYear());
        xmlCalendar1.setDay(dateTime1.getDayOfMonth());
        xmlCalendar1.setHour(dateTime1.getHourOfDay());
        xmlCalendar1.setMinute(dateTime1.getMinuteOfHour());
        xmlCalendar1.setSecond(dateTime1.getSecondOfMinute());
        xmlCalendar1.setMillisecond(dateTime1.getMillisOfSecond());
        xmlCalendar1.setTimezone(tz1.toTimeZone().getOffset(dateTime1.getMillis()) / 60000);

        FieldDesc fieldInfo = typeDesc.getField("tranDate");

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        AvroConverter<XMLGregorianCalendar, Long> converter1 =
                (AvroConverter<XMLGregorianCalendar, Long>) transducer.getValueConverter(fieldInfo);
        assertEquals(controlValue1,
                converter1.convertToAvro(xmlCalendar1));
        assertEquals(xmlCalendar1,
                converter1.convertToDatum(controlValue1));
    }

    @Test
    public void testJsonConverterComplexObject() throws Exception {
        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        Account account1 = new SimpleObjectComposer<>(Account.class).composeObject();

        RecordRef recordRef1 = new RecordRef();
        recordRef1.setInternalId("120001");
        recordRef1.setName("Talend France");

        RecordRef recordRef2 = new RecordRef();
        recordRef2.setInternalId("120002");
        recordRef2.setName("Talend China");

        RecordRefList recordRefList1 = new RecordRefList();
        recordRefList1.getRecordRef().add(recordRef1);
        recordRefList1.getRecordRef().add(recordRef2);

        account1.setSubsidiaryList(recordRefList1);

        BooleanCustomFieldRef customFieldRef1 = new BooleanCustomFieldRef();
        customFieldRef1.setInternalId("100001");
        customFieldRef1.setScriptId("custentity_field1");
        customFieldRef1.setValue(true);

        StringCustomFieldRef customFieldRef2 = new StringCustomFieldRef();
        customFieldRef2.setInternalId("100002");
        customFieldRef2.setScriptId("custentity_field2");
        customFieldRef2.setValue("test123");

        CustomFieldList customFieldList = new CustomFieldList();
        customFieldList.getCustomField().add(customFieldRef1);
        customFieldList.getCustomField().add(customFieldRef2);

        account1.setCustomFieldList(customFieldList);

        AvroConverter<Account, String> converter1 =
                (AvroConverter<Account, String>) transducer.getValueConverter(account1.getClass());

        String testJson1 = converter1.convertToAvro(account1);
        assertNotNull(testJson1);

        Account testAccount1 = converter1.convertToDatum(testJson1);
        assertNotNull(testAccount1);
    }

    @Test
    public void testJsonConverterNestedObjectConcreteType() throws Exception {
        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        ObjectMapper objectMapper = new ObjectMapper();

        RecordRef recordRef1 = new RecordRef();
        recordRef1.setInternalId("12345");
        recordRef1.setName("R&D");

        ObjectNode recordRefNode1 = JsonNodeFactory.instance.objectNode();
        recordRefNode1.set("name", JsonNodeFactory.instance.textNode("R&D"));
        recordRefNode1.set("internalId", JsonNodeFactory.instance.textNode("12345"));
        recordRefNode1.set("externalId", JsonNodeFactory.instance.nullNode());
        recordRefNode1.set("type", JsonNodeFactory.instance.nullNode());
        String recordRefJson1 = objectMapper.writer().writeValueAsString(recordRef1);

        FieldDesc fieldDesc = typeDesc.getField("department");
        AvroConverter<RecordRef, String> converter1 =
                (AvroConverter<RecordRef, String>) transducer.getValueConverter(fieldDesc);

        String testRecordRefJson1 = converter1.convertToAvro(recordRef1);
        assertNotNull(testRecordRefJson1);
        JsonNode testRecordRefNode1 = objectMapper.reader().readTree(testRecordRefJson1);
        assertTrue(testRecordRefNode1.has("name"));
        assertTrue(testRecordRefNode1.has("internalId"));
        assertTrue(testRecordRefNode1.has("externalId"));
        assertTrue(testRecordRefNode1.has("type"));
        assertEquals(recordRef1.getName(), testRecordRefNode1.get("name").asText());
        assertEquals(recordRef1.getInternalId(), testRecordRefNode1.get("internalId").asText());
        assertEquals(recordRef1.getExternalId(), testRecordRefNode1.get("externalId").asText(null));
        assertNull(testRecordRefNode1.get("type").asText(null));

        RecordRef testRecordRef1 = converter1.convertToDatum(recordRefJson1);
        assertNotNull(testRecordRef1);
        assertEquals(recordRef1.getName(), testRecordRef1.getName());
        assertEquals(recordRef1.getInternalId(), testRecordRef1.getInternalId());
        assertEquals(recordRef1.getExternalId(), testRecordRef1.getExternalId());
        assertEquals(recordRef1.getType(), testRecordRef1.getType());
    }

    @Test
    public void testJsonConverterNestedObjectPolymorphicType() throws Exception {
        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        BooleanCustomFieldRef customFieldRef1 = new BooleanCustomFieldRef();
        customFieldRef1.setInternalId("100001");
        customFieldRef1.setScriptId("custentity_field1");
        customFieldRef1.setValue(true);

        StringCustomFieldRef customFieldRef2 = new StringCustomFieldRef();
        customFieldRef2.setInternalId("100002");
        customFieldRef2.setScriptId("custentity_field2");
        customFieldRef2.setValue("test123");

        CustomFieldList customFieldList = new CustomFieldList();
        customFieldList.getCustomField().add(customFieldRef1);
        customFieldList.getCustomField().add(customFieldRef2);

        FieldDesc fieldDesc = typeDesc.getField("customFieldList");
        AvroConverter<CustomFieldList, String> converter1 =
                (AvroConverter<CustomFieldList, String>) transducer.getValueConverter(fieldDesc);

        String testJson1 = converter1.convertToAvro(customFieldList);
        assertNotNull(testJson1);

        ObjectNode node1 = JsonNodeFactory.instance.objectNode();
        ArrayNode list1 = JsonNodeFactory.instance.arrayNode();
        node1.set("customField", list1);
        ObjectNode customFieldNode1 = JsonNodeFactory.instance.objectNode();
        customFieldNode1.set(NsTypeResolverBuilder.TYPE_PROPERTY_NAME,
                JsonNodeFactory.instance.textNode("BooleanCustomFieldRef"));
        customFieldNode1.set("internalId", JsonNodeFactory.instance.textNode("100001"));
        customFieldNode1.set("scriptId", JsonNodeFactory.instance.textNode("custentity_field1"));
        customFieldNode1.set("value", JsonNodeFactory.instance.booleanNode(true));
        list1.add(customFieldNode1);
        ObjectNode customFieldNode2 = JsonNodeFactory.instance.objectNode();
        customFieldNode2.set(NsTypeResolverBuilder.TYPE_PROPERTY_NAME,
                JsonNodeFactory.instance.textNode("StringCustomFieldRef"));
        customFieldNode2.set("internalId", JsonNodeFactory.instance.textNode("100002"));
        customFieldNode2.set("scriptId", JsonNodeFactory.instance.textNode("custentity_field2"));
        customFieldNode2.set("value", JsonNodeFactory.instance.textNode("test123"));
        list1.add(customFieldNode2);

        CustomFieldList testCustomFieldList = converter1.convertToDatum(node1.toString());
        assertNotNull(testCustomFieldList);
        assertEquals(2, testCustomFieldList.getCustomField().size());
    }

    @Test
    public void testJsonConverterError() throws Exception {
        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        FieldDesc fieldDesc = typeDesc.getField("department");
        AvroConverter<RecordRef, String> converter1 =
                (AvroConverter<RecordRef, String>) transducer.getValueConverter(fieldDesc);
        try {
            converter1.convertToDatum("{name:'R&D',internalId:'12345',externalId:null,type:null}");
            fail("NetSuiteException expected");
        } catch (Exception e) {
            assertThat(e, instanceOf(NetSuiteException.class));
            NetSuiteException nsException = (NetSuiteException) e;
            assertNotNull(nsException.getCode());
            assertNotNull(nsException.getContext());
            assertNotNull(nsException.getContext().get(ExceptionContext.KEY_MESSAGE));
        }
    }

}
