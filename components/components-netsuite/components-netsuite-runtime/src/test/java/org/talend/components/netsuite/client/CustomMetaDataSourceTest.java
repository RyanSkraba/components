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

package org.talend.components.netsuite.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.netsuite.CustomFieldSpec;
import org.talend.components.netsuite.NetSuiteMockTestBase;
import org.talend.components.netsuite.client.model.BasicRecordType;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.test.client.model.TestRecordTypeEnum;

import com.netsuite.webservices.test.platform.core.Record;
import com.netsuite.webservices.test.platform.core.types.RecordType;
import com.netsuite.webservices.test.setup.customization.CustomFieldType;
import com.netsuite.webservices.test.setup.customization.CustomRecordCustomField;
import com.netsuite.webservices.test.setup.customization.CustomRecordType;
import com.netsuite.webservices.test.setup.customization.CustomRecordTypeFieldList;
import com.netsuite.webservices.test.setup.customization.TransactionBodyCustomField;
import com.netsuite.webservices.test.setup.customization.types.CustomizationFieldType;

/**
 *
 */
public class CustomMetaDataSourceTest extends NetSuiteMockTestBase {

    private DefaultCustomMetaDataSource customMetaDataSource;

    private TestCustomMetaDataRetriever customMetaDataRetriever;

    private NetSuiteClientService<?> clientService;

    @Override
    @Before
    public void setUp() throws Exception {
        installWebServiceMockTestFixture();

        super.setUp();

        clientService = webServiceMockTestFixture.getClientService();

        customMetaDataRetriever = new TestCustomMetaDataRetriever();
        customMetaDataSource = new DefaultCustomMetaDataSource(clientService, customMetaDataRetriever);
    }

    @Test
    public void testGetCustomFields() {
        Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs = createCustomFieldSpecs();

        customMetaDataRetriever.setCustomFieldSpecs(customFieldSpecs);

        RecordTypeInfo recordTypeInfo = clientService.getMetaDataSource().getRecordType(
                TestRecordTypeEnum.OPPORTUNITY.getTypeName());

        Map<String, CustomFieldDesc> customFieldDescMap = customMetaDataSource.getCustomFields(recordTypeInfo);
        assertNotNull(customFieldDescMap);

        for (CustomFieldSpec customFieldSpec : customFieldSpecs.values()) {
            CustomFieldDesc customFieldDesc = customFieldDescMap.get(customFieldSpec.getScriptId());
            assertNotNull(customFieldDesc);
            assertNotNull(customFieldDesc.getCustomizationRef());
            assertEquals(customFieldSpec.getScriptId(), customFieldDesc.getName());
            assertEquals(customFieldSpec.getInternalId(), customFieldDesc.getCustomizationRef().getInternalId());
        }

        CustomFieldDesc customFieldDesc = customFieldDescMap.get("custbody_field1");
        assertNotNull(customFieldDesc);
        assertEquals(CustomFieldRefType.BOOLEAN, customFieldDesc.getCustomFieldType());
        assertEquals(TestRecordTypeEnum.TRANSACTION_BODY_CUSTOM_FIELD.getType(), customFieldDesc.getCustomizationRef().getType());
    }

    @Test
    public void testGetCustomRecordTypes() throws Exception {
        Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customRecordFieldSpecs = createCustomRecordFieldSpecs();
        CustomRecordType customRecordType = createCustomRecordType(customRecordFieldSpecs);

        customMetaDataRetriever.setCustomRecordType(customRecordType);
        customMetaDataRetriever.setCustomRecordFieldSpecs(customRecordFieldSpecs);

        Collection<CustomRecordTypeInfo> recordTypeInfos = customMetaDataSource.getCustomRecordTypes();
        assertNotNull(recordTypeInfos);
        assertEquals(1, recordTypeInfos.size());

        CustomRecordTypeInfo recordTypeInfo1 = recordTypeInfos.iterator().next();
        assertNotNull(recordTypeInfo1.getName());
        assertNotNull(recordTypeInfo1.getCustomizationRef());
        assertEquals(customRecordType.getScriptId(), recordTypeInfo1.getName());
        assertEquals(customRecordType.getScriptId(), recordTypeInfo1.getCustomizationRef().getScriptId());
        assertEquals(customRecordType.getInternalId(), recordTypeInfo1.getCustomizationRef().getInternalId());
    }

    @Test
    public void testGetCustomRecordType() throws Exception {
        Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customRecordFieldSpecs = createCustomRecordFieldSpecs();
        CustomRecordType customRecordType = createCustomRecordType(customRecordFieldSpecs);

        customMetaDataRetriever.setCustomRecordType(customRecordType);
        customMetaDataRetriever.setCustomRecordFieldSpecs(customRecordFieldSpecs);

        CustomRecordTypeInfo recordTypeInfo = customMetaDataSource.getCustomRecordType(customRecordType.getScriptId());
        assertNotNull(recordTypeInfo);

        Map<String, CustomFieldDesc> customFieldDescMap = customMetaDataSource.getCustomFields(recordTypeInfo);
        assertNotNull(customFieldDescMap);

        for (CustomFieldSpec customFieldSpec : customRecordFieldSpecs.values()) {
            CustomFieldDesc customFieldDesc = customFieldDescMap.get(customFieldSpec.getScriptId());
            assertNotNull(customFieldDesc);
            assertNotNull(customFieldDesc.getCustomizationRef());
            assertEquals(customFieldSpec.getScriptId(), customFieldDesc.getName());
            assertEquals(customFieldSpec.getInternalId(), customFieldDesc.getCustomizationRef().getInternalId());
        }
    }

    @Test
    public void testEmptyCustomMetaDataSource() {
        EmptyCustomMetaDataSource emptyCustomMetaDataSource = new EmptyCustomMetaDataSource();
        assertTrue(emptyCustomMetaDataSource.getCustomRecordTypes().isEmpty());
        assertNull(emptyCustomMetaDataSource.getCustomRecordType("custrecord25"));
    }

    protected Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> createCustomFieldSpecs() {
        CustomFieldSpec<RecordType, CustomizationFieldType> customBodyField1 = new CustomFieldSpec(
                "custbody_field1", "1001",
                RecordType.TRANSACTION_BODY_CUSTOM_FIELD, TransactionBodyCustomField.class,
                CustomizationFieldType.CHECK_BOX, CustomFieldRefType.BOOLEAN,
                Arrays.asList("bodyOpportunity")
        );

        CustomFieldSpec<RecordType, CustomizationFieldType> customBodyField2 = new CustomFieldSpec(
                "custbody_field2", "1002",
                RecordType.TRANSACTION_BODY_CUSTOM_FIELD, TransactionBodyCustomField.class,
                CustomizationFieldType.FREE_FORM_TEXT, CustomFieldRefType.STRING,
                Arrays.asList("bodyOpportunity")
        );

        CustomFieldSpec<RecordType, CustomizationFieldType> customBodyField3 = new CustomFieldSpec(
                "custbody_field3", "1003",
                RecordType.TRANSACTION_BODY_CUSTOM_FIELD, TransactionBodyCustomField.class,
                CustomizationFieldType.DATETIME, CustomFieldRefType.DATE,
                Arrays.asList("bodyOpportunity")
        );

        Collection<CustomFieldSpec<RecordType, CustomizationFieldType>> specs = Arrays.asList(
                customBodyField1, customBodyField2, customBodyField3
        );
        Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> specMap = new HashMap<>();
        for (CustomFieldSpec<RecordType, CustomizationFieldType> spec : specs) {
            specMap.put(spec.getScriptId(), spec);
        }
        return specMap;
    }

    protected CustomRecordType createCustomRecordType(Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs) throws Exception {

        CustomRecordType customRecordType = new CustomRecordType();
        customRecordType.setScriptId("custrecord1");
        customRecordType.setInternalId("201");

        Map<String, CustomFieldType> customFieldTypeMap = createCustomFieldTypes(customFieldSpecs);
        customRecordType.setCustomFieldList(new CustomRecordTypeFieldList());
        for (CustomFieldType customFieldType : customFieldTypeMap.values()) {
            customRecordType.getCustomFieldList().getCustomField().add((CustomRecordCustomField) customFieldType);
        }

        return customRecordType;
    }

    protected Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> createCustomRecordFieldSpecs() {
        CustomFieldSpec<RecordType, CustomizationFieldType> customRecordField1 = new CustomFieldSpec(
                "custrec_field1", "2001",
                RecordType.CUSTOM_RECORD_CUSTOM_FIELD, CustomRecordCustomField.class,
                CustomizationFieldType.FREE_FORM_TEXT, CustomFieldRefType.STRING, null
        );

        CustomFieldSpec<RecordType, CustomizationFieldType> customRecordField2 = new CustomFieldSpec(
                "custrec_field2", "2002",
                RecordType.CUSTOM_RECORD_CUSTOM_FIELD, CustomRecordCustomField.class,
                CustomizationFieldType.DATETIME, CustomFieldRefType.DATE, null
        );

        Collection<CustomFieldSpec<RecordType, CustomizationFieldType>> specs = Arrays.asList(
                customRecordField1, customRecordField2
        );
        Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> specMap = new HashMap<>();
        for (CustomFieldSpec<RecordType, CustomizationFieldType> spec : specs) {
            specMap.put(spec.getScriptId(), spec);
        }
        return specMap;
    }

    protected class TestCustomMetaDataRetriever implements DefaultCustomMetaDataSource.CustomMetaDataRetriever {

        private Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs;
        private CustomRecordType customRecordType;
        private Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customRecordFieldSpecs;

        public TestCustomMetaDataRetriever() {
        }

        public Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> getCustomFieldSpecs() {
            return customFieldSpecs;
        }

        public void setCustomFieldSpecs(Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs) {
            this.customFieldSpecs = customFieldSpecs;
        }

        public CustomRecordType getCustomRecordType() {
            return customRecordType;
        }

        public void setCustomRecordType(CustomRecordType customRecordType) {
            this.customRecordType = customRecordType;
        }

        public Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> getCustomRecordFieldSpecs() {
            return customRecordFieldSpecs;
        }

        public void setCustomRecordFieldSpecs(
                Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customRecordFieldSpecs) {
            this.customRecordFieldSpecs = customRecordFieldSpecs;
        }

        @Override
        public List<NsRef> retrieveCustomizationIds(BasicRecordType type) throws NetSuiteException {
            try {
                List<NsRef> customizationRefs = new ArrayList<>();

                if (customFieldSpecs != null) {
                    Map<String, NsRef> customizationRefMap = createCustomFieldCustomizationRefs(customFieldSpecs);
                    for (String scriptId : customFieldSpecs.keySet()) {
                        NsRef customizationRef = customizationRefMap.get(scriptId);
                        if (type.getType().equals(customizationRef.getType())) {
                            customizationRefs.add(customizationRef);
                        }
                    }
                }

                if (TestRecordTypeEnum.CUSTOM_RECORD_TYPE.getType().equals(type.getType()) &&
                        customRecordType != null) {
                    customizationRefs.add(createCustomRecordCustomizationRef(customRecordType));
                }

                return customizationRefs;
            } catch (Exception e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

        @Override
        public List<?> retrieveCustomizations(List<NsRef> nsCustomizationRefs) throws NetSuiteException {
            try {
                List<Record> customizations = new ArrayList<>();

                if (customFieldSpecs != null) {
                    Map<String, CustomFieldType> customFieldTypeMap = createCustomFieldTypes(customFieldSpecs);
                    for (NsRef customizationRef : nsCustomizationRefs) {
                        if (customizationRef.getRefType() == RefType.CUSTOMIZATION_REF) {
                            if (customFieldTypeMap.containsKey(customizationRef.getScriptId())) {
                                CustomFieldType fieldType = customFieldTypeMap.get(customizationRef.getScriptId());
                                customizations.add(fieldType);
                            }
                        }
                    }
                }

                if (customRecordType != null) {
                    NsRef customizationRef = createCustomRecordCustomizationRef(customRecordType);
                    for (NsRef ref : nsCustomizationRefs) {
                        if (customizationRef.equals(ref)) {
                            customizations.add(customRecordType);
                        }
                    }
                }

                return customizations;
            } catch (Exception e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

        @Override
        public Map<String, CustomFieldDesc> retrieveCustomRecordCustomFields(RecordTypeDesc recordType,
                NsRef nsCustomizationRef) throws NetSuiteException {
            try {
                Map<String, CustomFieldDesc> customFieldDescMap = Collections.emptyMap();
                if (customRecordType != null && customRecordFieldSpecs != null && !customRecordFieldSpecs.isEmpty()) {
                    NsRef customizationRef = createCustomRecordCustomizationRef(customRecordType);
                    if (nsCustomizationRef.equals(customizationRef)) {
                        Map<String, CustomFieldType> customFieldTypeMap = createCustomFieldTypes(customRecordFieldSpecs);
                        customFieldDescMap = DefaultCustomMetaDataSource.createCustomFieldDescMap(clientService,
                                recordType, BasicRecordType.getByType(nsCustomizationRef.getType()),
                                new ArrayList<Object>(customFieldTypeMap.values()));
                    }
                }

                return customFieldDescMap;
            } catch (Exception e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }
    }
}
