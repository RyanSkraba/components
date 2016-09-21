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

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.fullexample.FullExampleProperties;
//import org.talend.components.salesforce.SalesforceConnectionProperties;
//import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.properties.Properties;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
public class TestJsonSerialize {

    @Autowired
    Jackson2ObjectMapperBuilder jacksonBuilder;

    @Test
    public void TestSerializationSizes() throws IOException {
        FullExampleProperties fep = new FullExampleProperties(null);
        fep.stringProp.setValue("stringProp");
        fep.tableProp.colEnum.setValue(FullExampleProperties.TableProperties.ColEnum.FOO);
        fep.tableProp.colString.setValue("foooo");
        String jsonioString = fep.toSerialized();
        System.out.println("jsonio:" + FileUtils.byteCountToDisplaySize(jsonioString.getBytes().length));

        FullExampleProperties fepCopy = Properties.Helper.fromSerializedPersistent(jsonioString,
                FullExampleProperties.class).object;
        assertNull(fepCopy.hiddenTextProp.getValue());
        assertEquals("foooo", fep.tableProp.colString.getValue());
        assertEquals("foooo", fepCopy.tableProp.colString.getValue());
    }

}
