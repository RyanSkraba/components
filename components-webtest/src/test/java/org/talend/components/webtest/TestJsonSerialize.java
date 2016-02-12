// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.webtest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import com.fasterxml.jackson.core.JsonGenerator;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
public class TestJsonSerialize {

    @Autowired
    Jackson2ObjectMapperBuilder jacksonBuilder;

    @Test
    public void TestSerializationSizes() throws IOException {
        TSalesforceInputProperties tsip = (TSalesforceInputProperties) new TSalesforceInputProperties(null).init();
        tsip.connection.loginType.setValue(SalesforceConnectionProperties.LOGIN_BASIC);
        tsip.connection.userPassword.userId.setValue("foooo");
        StringWriter jsonJacksonStringWriter = new StringWriter();
        JsonGenerator jacksonGenerator = jacksonBuilder.build().getFactory().createGenerator(jsonJacksonStringWriter);
        jacksonGenerator.writeObject(tsip);
        System.out.println("jackson:" + FileUtils.byteCountToDisplaySize(jsonJacksonStringWriter.toString().getBytes().length));
        // jsonio
        String jsonioString = tsip.toSerialized();
        System.out.println("jsonio:" + FileUtils.byteCountToDisplaySize(jsonioString.getBytes().length));
        TSalesforceInputProperties salesforceInputProperties = ComponentProperties.fromSerialized(jsonioString,
                TSalesforceInputProperties.class).properties;
        assertNull(salesforceInputProperties.connection.proxy.host.getValue());
        assertEquals("foooo", tsip.connection.userPassword.userId.getValue());
    }

}
