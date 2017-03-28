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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.Schema;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.junit.Test;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.NsObjectTransducer;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.input.NsObjectInputTransducer;
import org.talend.components.netsuite.v2016_2.client.NetSuiteClientServiceImpl;

import com.netsuite.webservices.v2016_2.lists.accounting.types.AccountType;
import com.netsuite.webservices.v2016_2.lists.accounting.types.ConsolidatedRate;
import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;

/**
 *
 */
public class ValueConverterTest extends NetSuiteMockTestBase {

    private NetSuiteClientService<NetSuitePortType> clientService = new NetSuiteClientServiceImpl();

    @Test
    public void testEnumConverter() throws Exception {
        TypeDesc typeDesc = clientService.getBasicMetaData().getTypeInfo("Account");

        Schema s = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, s, typeDesc.getTypeName());

        FieldDesc fieldDesc = typeDesc.getField("AcctType");
        NsObjectTransducer.ValueConverter<Enum<AccountType>, String> converter1 =
                (NsObjectTransducer.ValueConverter<Enum<AccountType>, String>) transducer.getValueConverter(fieldDesc);
        assertEquals(AccountType.ACCOUNTS_PAYABLE.value(),
                converter1.convertInput(AccountType.ACCOUNTS_PAYABLE));
        assertEquals(AccountType.ACCOUNTS_PAYABLE,
                converter1.convertOutput(AccountType.ACCOUNTS_PAYABLE.value()));

        fieldDesc = typeDesc.getField("GeneralRate");
        assertNotNull(fieldDesc);
        NsObjectTransducer.ValueConverter<Enum<ConsolidatedRate>, String> converter2 =
                (NsObjectTransducer.ValueConverter<Enum<ConsolidatedRate>, String>) transducer.getValueConverter(fieldDesc);
        assertEquals(ConsolidatedRate.HISTORICAL.value(),
                converter2.convertInput(ConsolidatedRate.HISTORICAL));
        assertEquals(ConsolidatedRate.HISTORICAL,
                converter2.convertOutput(ConsolidatedRate.HISTORICAL.value()));
    }

    @Test
    public void testXMLGregorianCalendarConverter() throws Exception {
        TypeDesc typeDesc = clientService.getBasicMetaData().getTypeInfo("Account");

        Schema s = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

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

        FieldDesc fieldInfo = typeDesc.getField("TranDate");
        Schema.Field f = s.getField(fieldInfo.getName());
        assertNotNull(f);

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, s, typeDesc.getTypeName());

        NsObjectTransducer.ValueConverter<XMLGregorianCalendar, Long> converter1 =
                (NsObjectTransducer.ValueConverter<XMLGregorianCalendar, Long>) transducer.getValueConverter(fieldInfo);
        assertEquals(controlValue1,
                converter1.convertInput(xmlCalendar1));
        assertEquals(xmlCalendar1,
                converter1.convertOutput(controlValue1));
    }
}
