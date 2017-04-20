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

package org.talend.components.netsuite.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 */
public class TestUtils {

    public static Properties loadPropertiesFromLocation(String location) throws IOException {
        URI uri = URI.create(location);
        if (uri.getScheme() == null) {
            try {
                uri = new URI("classpath", location, null);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
        return loadPropertiesFromLocation(uri);
    }

    public static Properties loadProperties(Properties source, Collection<String> propertyNames) {
        Properties properties = new Properties();
        for (String propertyName : propertyNames) {
            String value = source.getProperty(propertyName);
            if (value != null) {
                properties.setProperty(propertyName, value);
            }
        }
        return properties;
    }

    public static Properties loadPropertiesFromLocation(URI location) throws IOException {
        InputStream stream;
        if (location.getScheme().equals("classpath")) {
            stream = TestUtils.class.getResourceAsStream(location.getSchemeSpecificPart());
        } else {
            stream = location.toURL().openStream();
        }
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } finally {
            stream.close();
        }
        return properties;
    }

    public CustomRecordTypeInfo readCustomRecord(BasicMetaData basicMetaData, JsonNode node) {
        String scriptId = node.get("scriptId").asText();
        String internalId = node.get("internalId").asText();
        String customizationType = node.get("customizationType").asText();
        String recordType = node.get("recordType").asText();

        NsRef ref = new NsRef();
        ref.setRefType(RefType.CUSTOMIZATION_REF);
        ref.setScriptId(scriptId);
        ref.setInternalId(internalId);
        ref.setType(customizationType);

        RecordTypeDesc recordTypeDesc = basicMetaData.getRecordType(recordType);
        CustomRecordTypeInfo recordTypeInfo = new CustomRecordTypeInfo(scriptId, recordTypeDesc, ref);

        return recordTypeInfo;
    }

    public static Map<String, CustomFieldDesc> readCustomFields(JsonNode node) {
        Map<String, CustomFieldDesc> customFieldDescMap = new HashMap<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode fieldNode = node.get(i);
            CustomFieldDesc customFieldDesc = readCustomField(fieldNode);
            customFieldDescMap.put(customFieldDesc.getName(), customFieldDesc);
        }
        return customFieldDescMap;
    }

    public static CustomFieldDesc readCustomField(JsonNode node) {
        String scriptId = node.get("scriptId").asText();
        String internalId = node.get("internalId").asText();
        String customizationType = node.get("customizationType").asText();
        String type = node.get("valueType").asText();

        NsRef ref = new NsRef();
        ref.setRefType(RefType.CUSTOMIZATION_REF);
        ref.setScriptId(scriptId);
        ref.setInternalId(internalId);
        ref.setType(customizationType);

        CustomFieldRefType customFieldRefType = CustomFieldRefType.valueOf(type);

        CustomFieldDesc fieldDesc = new CustomFieldDesc();
        fieldDesc.setCustomFieldType(customFieldRefType);
        fieldDesc.setRef(ref);
        fieldDesc.setName(scriptId);
        fieldDesc.setValueType(NetSuiteDatasetRuntimeImpl.getCustomFieldValueClass(customFieldRefType));
        fieldDesc.setNullable(true);

        return fieldDesc;
    }

}
