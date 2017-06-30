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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl.getNsFieldByName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.talend.components.netsuite.client.CustomMetaDataSource;
import org.talend.components.netsuite.client.EmptyCustomMetaDataSource;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.test.client.TestNetSuiteClientService;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.test.TestUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.di.DiSchemaConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsuite.webservices.test.platform.NetSuitePortType;

/**
 *
 */
public class NetSuiteDatasetRuntimeTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private NetSuiteClientService<NetSuitePortType> clientService = new TestNetSuiteClientService();

    @Test
    public void testGetSchemaForRecordBasic() throws Exception {
        TypeDesc typeDesc = clientService.getBasicMetaData().getTypeInfo("Account");

        Schema s = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

        assertThat(s.getType(), is(Schema.Type.RECORD));
        assertThat(s.getName(), is("Account"));
        assertThat(s.getFields(), hasSize(typeDesc.getFields().size()));
        assertThat(s.getObjectProps().keySet(), empty());

        FieldDesc fieldDesc = typeDesc.getField("acctType");
        Schema.Field f = getNsFieldByName(s, fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.STRING, Schema.Type.NULL));
        assertThat(f.getObjectProps().keySet(), containsInAnyOrder(
                DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME,
                DiSchemaConstants.TALEND6_COLUMN_SOURCE_TYPE
        ));
        assertThat(f.getProp(DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME), is(fieldDesc.getName()));
        assertThat(f.schema().getObjectProps().keySet(), empty());

        fieldDesc = typeDesc.getField("acctName");
        f = getNsFieldByName(s, fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.STRING, Schema.Type.NULL));
        assertThat(f.getObjectProps().keySet(), containsInAnyOrder(
                DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME,
                DiSchemaConstants.TALEND6_COLUMN_SOURCE_TYPE
        ));
        assertThat(f.getProp(DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME), is(fieldDesc.getName()));
        assertThat(f.schema().getObjectProps().keySet(), empty());

        fieldDesc = typeDesc.getField("inventory");
        f = getNsFieldByName(s, fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.BOOLEAN, Schema.Type.NULL));
        assertThat(f.getObjectProps().keySet(), containsInAnyOrder(
                DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME,
                DiSchemaConstants.TALEND6_COLUMN_SOURCE_TYPE
        ));
        assertThat(f.getProp(DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME), is(fieldDesc.getName()));
        assertThat(f.schema().getObjectProps().keySet(), empty());

        fieldDesc = typeDesc.getField("tranDate");
        f = getNsFieldByName(s, fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.LONG, Schema.Type.NULL));
        assertThat(f.getObjectProps().keySet(), containsInAnyOrder(
                SchemaConstants.TALEND_COLUMN_PATTERN,
                DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME,
                DiSchemaConstants.TALEND6_COLUMN_SOURCE_TYPE
        ));
        assertThat(f.getProp(SchemaConstants.TALEND_COLUMN_PATTERN), is("yyyy-MM-dd'T'HH:mm:ss'.000Z'"));
        assertThat(f.getProp(DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME), is(fieldDesc.getName()));
    }

    @Test
    public void testGetSchemaForRecordWithCustomFields() throws Exception {
        final CustomMetaDataSource customMetaDataSource = new EmptyCustomMetaDataSource() {
            @Override public Map<String, CustomFieldDesc> getCustomFields(RecordTypeInfo recordTypeInfo) {
                try {
                    if (recordTypeInfo.getName().equals("Account")) {
                        JsonNode fieldListNode = objectMapper.readTree(NetSuiteDatasetRuntimeTest.class.getResource(
                                "/test-data/customFields-1.json"));
                        Map<String, CustomFieldDesc> customFieldDescMap =
                                TestUtils.readCustomFields(fieldListNode);
                        return customFieldDescMap;
                    }
                    return null;
                } catch (IOException e) {
                    throw new NetSuiteException(e.getMessage(), e);
                }
            }
        };

        MetaDataSource metaDataSource = clientService.createDefaultMetaDataSource();
        metaDataSource.setCustomMetaDataSource(customMetaDataSource);

        NetSuiteDatasetRuntimeImpl datasetRuntime = new NetSuiteDatasetRuntimeImpl(metaDataSource);

        TypeDesc typeDesc = metaDataSource.getTypeInfo("Account");

        Schema s = datasetRuntime.getSchema(typeDesc.getTypeName());

        assertThat(s.getType(), is(Schema.Type.RECORD));
        assertThat(s.getName(), is("Account"));
        assertThat(s.getFields(), hasSize(typeDesc.getFields().size()));
        assertThat(s.getObjectProps().keySet(), Matchers.<String>empty());

        CustomFieldDesc fieldDesc = (CustomFieldDesc) typeDesc.getField("custom_field_1");
        Schema.Field f = getNsFieldByName(s, fieldDesc.getName());
        assertUnionType(f.schema(), Arrays.asList(Schema.Type.STRING, Schema.Type.NULL));
        assertThat(f.getObjectProps().keySet(), containsInAnyOrder(
                DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME,
                DiSchemaConstants.TALEND6_COLUMN_SOURCE_TYPE,
                NetSuiteSchemaConstants.NS_CUSTOM_FIELD,
                NetSuiteSchemaConstants.NS_CUSTOM_FIELD_SCRIPT_ID,
                NetSuiteSchemaConstants.NS_CUSTOM_FIELD_INTERNAL_ID,
                NetSuiteSchemaConstants.NS_CUSTOM_FIELD_CUSTOMIZATION_TYPE,
                NetSuiteSchemaConstants.NS_CUSTOM_FIELD_TYPE
        ));
        assertThat(f.getProp(DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME), is(fieldDesc.getName()));
        CustomFieldDesc customFieldDesc = NetSuiteDatasetRuntimeImpl.readCustomField(f);
        assertThat(customFieldDesc.getName(), is(fieldDesc.getName()));
    }

    @Test
    public void testGetSearchFieldOperators() {
        NetSuiteDatasetRuntime dataSetRuntime = new NetSuiteDatasetRuntimeImpl(clientService.getMetaDataSource());
        List<String> operators = dataSetRuntime.getSearchFieldOperators();
        for (String operator : operators) {
            assertNotNull(operator);
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
