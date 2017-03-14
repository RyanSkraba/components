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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.netsuite.NsObjectTransducer;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.TypeUtils;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;

/**
 *
 */
public class NsObjectOutputTransducer extends NsObjectTransducer {
    protected String typeName;
    protected boolean reference;

    protected TypeDesc typeDesc;
    protected Map<String, FieldDesc> fieldMap;
    protected BeanInfo beanInfo;

    protected RefType refType;

    protected String referencedTypeName;
    protected RecordTypeInfo referencedRecordTypeInfo;

    public NsObjectOutputTransducer(NetSuiteClientService<?> clientService, String typeName) {
        this(clientService, typeName, false);
    }

    public NsObjectOutputTransducer(NetSuiteClientService<?> clientService, String typeName, boolean reference) {
        super(clientService);

        this.typeName = typeName;
        this.reference = reference;
    }

    protected void prepare() {
        if (typeDesc != null) {
            return;
        }

        if (reference) {
            referencedTypeName = typeName;
            referencedRecordTypeInfo = clientService.getRecordType(referencedTypeName);
            refType = RefType.RECORD_REF;
            typeDesc = clientService.getTypeInfo(refType.getTypeName());
        } else {
            typeDesc = clientService.getTypeInfo(typeName);
        }

        beanInfo = Beans.getBeanInfo(typeDesc.getTypeClass());
        fieldMap = typeDesc.getFieldMap();
    }

    public Object write(IndexedRecord indexedRecord) {
        prepare();

        Schema schema = indexedRecord.getSchema();

        try {
            Object nsObject = clientService.getBasicMetaData().createInstance(typeDesc.getTypeName());

            Set<String> nullFieldNames = new HashSet<>();

            for (Schema.Field field : schema.getFields()) {
                String fieldName = field.name();
                FieldDesc fieldDesc = fieldMap.get(fieldName);

                if (fieldDesc == null) {
                    continue;
                }

                Object value = indexedRecord.get(field.pos());
                Object result = writeField(nsObject, fieldDesc, value);
                if (result == null) {
                    nullFieldNames.add(fieldDesc.getInternalName());
                }
            }

            if (!nullFieldNames.isEmpty() && beanInfo.getProperty("nullFieldList") != null) {
                Object nullFieldListWrapper = getSimpleProperty(nsObject, "nullFieldList");
                if (nullFieldListWrapper == null) {
                    nullFieldListWrapper = clientService.getBasicMetaData()
                            .createInstance("NullField");
                    setSimpleProperty(nsObject, "nullFieldList", nullFieldListWrapper);
                }
                List<String> nullFields = (List<String>) getSimpleProperty(nullFieldListWrapper, "name");
                nullFields.addAll(nullFieldNames);
            }

            if (reference) {
                if (refType == RefType.RECORD_REF) {
                    FieldDesc recTypeFieldDesc = typeDesc.getField("Type");
                    RecordTypeDesc recordTypeDesc = referencedRecordTypeInfo.getRecordType();
                    writeField(nsObject, recTypeFieldDesc, recordTypeDesc.getType());
                }
            }

            return nsObject;
        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }
}
