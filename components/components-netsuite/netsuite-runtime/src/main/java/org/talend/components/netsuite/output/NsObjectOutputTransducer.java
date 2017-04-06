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

package org.talend.components.netsuite.output;

import static org.talend.components.netsuite.client.model.beans.Beans.getSimpleProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.NsObjectTransducer;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;

/**
 *
 */
public class NsObjectOutputTransducer extends NsObjectTransducer {
    protected String typeName;
    protected boolean reference;

    protected TypeDesc typeDesc;
    protected RecordTypeInfo recordTypeInfo;

    public NsObjectOutputTransducer(NetSuiteClientService<?> clientService, String typeName) {
        super(clientService);

        this.typeName = typeName;
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

    protected void prepare() {
        if (typeDesc != null) {
            return;
        }

        recordTypeInfo = metaDataSource.getRecordType(typeName);
        if (reference) {
            typeDesc = metaDataSource.getTypeInfo(recordTypeInfo.getRefType().getTypeName());
        } else {
            typeDesc = metaDataSource.getTypeInfo(typeName);
        }
    }

    public NsRef getRef(IndexedRecord indexedRecord) {
        prepare();

        Schema schema = indexedRecord.getSchema();
        return getRef(schema, indexedRecord);
    }

    protected NsRef getRef(Schema schema, IndexedRecord indexedRecord) {
        if (recordTypeInfo == null) {
            return null;
        }

        Schema.Field internalIdField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(schema, "internalId");
        String internalId = internalIdField != null ? (String) indexedRecord.get(internalIdField.pos()) : null;

        Schema.Field externalIdField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(schema, "externalId");
        String externalId = externalIdField != null ? (String) indexedRecord.get(externalIdField.pos()) : null;

        if (internalId == null && externalId == null) {
            return null;
        }

        NsRef ref;
        if (recordTypeInfo instanceof CustomRecordTypeInfo) {
            Schema.Field scriptIdField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(schema, "scriptId");
            String scriptId = scriptIdField != null ? (String) indexedRecord.get(scriptIdField.pos()) : null;
            ref = ((CustomRecordTypeInfo) recordTypeInfo).createRef(internalId, externalId, scriptId);
        } else {
            ref = recordTypeInfo.createRef(internalId, externalId);
        }
        return ref;
    }

    public Object write(IndexedRecord indexedRecord) {
        prepare();

        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();
        BeanInfo beanInfo = Beans.getBeanInfo(typeDesc.getTypeClass());

        Schema schema = indexedRecord.getSchema();

        try {
            Object nsObject = clientService.getBasicMetaData().createInstance(typeDesc.getTypeName());

            Set<String> nullFieldNames = new HashSet<>();

            Map<String, Object> customFieldMap = Collections.emptyMap();

            if (!reference && beanInfo.getProperty("customFieldList") != null) {
                customFieldMap = new HashMap<>();

                Object customFieldListWrapper = getSimpleProperty(nsObject, "customFieldList");
                if (customFieldListWrapper != null) {
                    List<Object> customFieldList = (List<Object>) getSimpleProperty(customFieldListWrapper, "customField");
                    for (Object customField : customFieldList) {
                        String scriptId = (String) getSimpleProperty(customField, "scriptId");
                        customFieldMap.put(scriptId, customField);
                    }
                }
            }

            for (Schema.Field field : schema.getFields()) {
                String nsFieldName = NetSuiteDatasetRuntimeImpl.getNsFieldName(field);

                FieldDesc fieldDesc = fieldMap.get(nsFieldName);
                if (fieldDesc == null) {
                    continue;
                }

                Object value = indexedRecord.get(field.pos());

                writeField(nsObject, fieldDesc, customFieldMap, nullFieldNames, value);
            }

            if (!nullFieldNames.isEmpty() && beanInfo.getProperty("nullFieldList") != null) {
                Object nullFieldListWrapper = clientService.getBasicMetaData()
                        .createInstance("NullField");
                setSimpleProperty(nsObject, "nullFieldList", nullFieldListWrapper);
                List<String> nullFields = (List<String>) getSimpleProperty(nullFieldListWrapper, "name");
                nullFields.addAll(nullFieldNames);
            }

            if (reference) {
                if (recordTypeInfo.getRefType() == RefType.RECORD_REF) {
                    FieldDesc recTypeFieldDesc = typeDesc.getField("type");
                    RecordTypeDesc recordTypeDesc = recordTypeInfo.getRecordType();
                    writeSimpleField(nsObject, recTypeFieldDesc.asSimple(), false, nullFieldNames, recordTypeDesc.getType());
                }
            }

            return nsObject;
        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }

}
