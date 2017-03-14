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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.v2016_2.client.NetSuiteClientServiceImpl;
import org.talend.daikon.avro.SchemaConstants;

import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;

/**
 *
 */
public class NetSuiteDatasetRuntimeTest extends NetSuiteMockTestBase {

    private NetSuiteClientService<NetSuitePortType> clientService = new NetSuiteClientServiceImpl();

    @Test
    public void testInferSchemaForRecordBasic() throws Exception {
        TypeDesc typeDesc = clientService.getBasicMetaData().getTypeInfo("Account");

        Schema s = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());
//        System.out.println(s);

        assertThat(s.getType(), is(Schema.Type.RECORD));
        assertThat(s.getName(), is("Account"));
        assertThat(s.getFields(), hasSize(typeDesc.getFields().size()));
        assertThat(s.getObjectProps().keySet(), empty());

        FieldDesc fieldDesc = typeDesc.getField("AcctType");
        Schema.Field f = s.getField(fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.STRING, Schema.Type.NULL));
        assertThat(f.schema().getObjectProps().keySet(), empty());

        fieldDesc = typeDesc.getField("AcctName");
        f = s.getField(fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.STRING, Schema.Type.NULL));
        assertThat(f.schema().getObjectProps().keySet(), empty());

        fieldDesc = typeDesc.getField("Inventory");
        f = s.getField(fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.BOOLEAN, Schema.Type.NULL));
        assertThat(f.schema().getObjectProps().keySet(), empty());

        fieldDesc = typeDesc.getField("TranDate");
        f = s.getField(fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.LONG, Schema.Type.NULL));
        assertThat(f.getObjectProps().keySet(), containsInAnyOrder(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertThat(f.getProp(SchemaConstants.TALEND_COLUMN_PATTERN), is("yyyy-MM-dd'T'HH:mm:ss'.000Z'"));
    }

    @Test
    public void testGetSearchFieldOperators() {
        NetSuiteDatasetRuntime dataSetRuntime = new NetSuiteDatasetRuntimeImpl(clientService);
        List<String> operators = dataSetRuntime.getSearchFieldOperators();
        for (String operator : operators) {
            assertNotNull(operator);
//            System.out.println(operator);
        }
    }

    private static void assertUnionType(Schema schema, List<Schema.Type> types) {
        assertThat(schema.getType(), is(Schema.Type.UNION));
        List<Schema> members = schema.getTypes();
        List<Schema.Type> memberTypes = new ArrayList<>(members.size());
        for (Schema member : members) {
            memberTypes.add(member.getType());
        }
        assertThat(types, is(memberTypes));
    }
}
