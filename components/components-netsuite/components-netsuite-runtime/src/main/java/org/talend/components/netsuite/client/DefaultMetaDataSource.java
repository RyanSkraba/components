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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.BasicRecordType;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.CustomTransactionTypeInfo;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;

/**
 * Implementation of <code>MetaDataSource</code> which retrieves customization related meta data
 * from NetSuite web service via <code>NetSuiteClientService</code>.
 *
 * @see NetSuiteClientService
 * @see BasicMetaData
 * @see CustomMetaDataSource
 */
public class DefaultMetaDataSource implements MetaDataSource {
    protected NetSuiteClientService<?> clientService;
    protected boolean customizationEnabled = true;
    protected CustomMetaDataSource customMetaDataSource;

    public DefaultMetaDataSource(NetSuiteClientService<?> clientService) {
        this.clientService = clientService;

        customMetaDataSource = clientService.createDefaultCustomMetaDataSource();
    }

    public NetSuiteClientService<?> getClientService() {
        return clientService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCustomizationEnabled() {
        return customizationEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCustomizationEnabled(boolean customizationEnabled) {
        this.customizationEnabled = customizationEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BasicMetaData getBasicMetaData() {
        return clientService.getBasicMetaData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CustomMetaDataSource getCustomMetaDataSource() {
        return customMetaDataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCustomMetaDataSource(CustomMetaDataSource customMetaDataSource) {
        this.customMetaDataSource = customMetaDataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<RecordTypeInfo> getRecordTypes() {
        List<RecordTypeInfo> recordTypes = new ArrayList<>();

        Collection<RecordTypeDesc> standardRecordTypes = clientService.getBasicMetaData().getRecordTypes();
        for (RecordTypeDesc recordType : standardRecordTypes) {
            recordTypes.add(new RecordTypeInfo(recordType));
        }

        if (customizationEnabled) {
            recordTypes.addAll(customMetaDataSource.getCustomRecordTypes());
        }

        return recordTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<NamedThing> getSearchableTypes() {
        List<NamedThing> searchableTypes = new ArrayList<>(256);

        Collection<RecordTypeInfo> recordTypes = getRecordTypes();

        for (RecordTypeInfo recordTypeInfo : recordTypes) {
            RecordTypeDesc recordTypeDesc = recordTypeInfo.getRecordType();
            if (recordTypeDesc.getSearchRecordType() != null) {
                SearchRecordTypeDesc searchRecordType = clientService.getBasicMetaData().getSearchRecordType(recordTypeDesc);
                if (searchRecordType != null) {
                    searchableTypes.add(new SimpleNamedThing(recordTypeInfo.getName(), recordTypeInfo.getDisplayName()));
                }
            }
        }

        return searchableTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeDesc getTypeInfo(final Class<?> clazz) {
        return getTypeInfo(clazz.getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeDesc getTypeInfo(final String typeName) {
        TypeDesc baseTypeDesc;
        String targetTypeName = null;
        Class<?> targetTypeClass;
        List<FieldDesc> baseFieldDescList;

        RecordTypeInfo recordTypeInfo = getRecordType(typeName);
        if (recordTypeInfo != null) {
            if (recordTypeInfo instanceof CustomRecordTypeInfo || recordTypeInfo instanceof CustomTransactionTypeInfo) {
                baseTypeDesc = clientService.getBasicMetaData().getTypeInfo(recordTypeInfo.getRecordType().getTypeName());
                targetTypeName = recordTypeInfo.getName();
            } else {
                baseTypeDesc = clientService.getBasicMetaData().getTypeInfo(typeName);
            }
        } else {
            baseTypeDesc = clientService.getBasicMetaData().getTypeInfo(typeName);
        }

        if (targetTypeName == null) {
            targetTypeName = baseTypeDesc.getTypeName();
        }
        targetTypeClass = baseTypeDesc.getTypeClass();
        baseFieldDescList = baseTypeDesc.getFields();

        List<FieldDesc> resultFieldDescList = new ArrayList<>(baseFieldDescList.size() + 10);

        // Add basic fields except field list containers (custom field list, null field list)
        for (FieldDesc fieldDesc : baseFieldDescList) {
            String fieldName = fieldDesc.getName();
            // Custom field list is stored as it is, since we had an issue with retrieving it from NetSuite via SOAP calls
            if (!(recordTypeInfo instanceof CustomTransactionTypeInfo) &&
                    (fieldName.equals("customFieldList") || fieldName.equals("nullFieldList"))) {
                continue;
            }
            resultFieldDescList.add(fieldDesc);
        }

        if (recordTypeInfo != null && !(recordTypeInfo instanceof CustomTransactionTypeInfo)) {
            if (customizationEnabled) {
                // Add custom fields
                Map<String, CustomFieldDesc> customFieldMap =
                        customMetaDataSource.getCustomFields(recordTypeInfo);
                for (CustomFieldDesc fieldInfo : customFieldMap.values()) {
                    resultFieldDescList.add(fieldInfo);
                }
            }
        }

        return new TypeDesc(targetTypeName, targetTypeClass, resultFieldDescList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordTypeInfo getRecordType(String typeName) {
        RecordTypeDesc recordType = clientService.getBasicMetaData().getRecordType(typeName);
        if (recordType != null) {
            return new RecordTypeInfo(recordType);
        }
        if (customizationEnabled) {
            return customMetaDataSource.getCustomRecordType(typeName);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchRecordTypeDesc getSearchRecordType(String recordTypeName) {
        SearchRecordTypeDesc searchRecordType = clientService.getBasicMetaData().getSearchRecordType(recordTypeName);
        if (searchRecordType != null) {
            return searchRecordType;
        }
        RecordTypeInfo recordTypeInfo = getRecordType(recordTypeName);
        if (recordTypeInfo != null) {
            return getSearchRecordType(recordTypeInfo.getRecordType());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchRecordTypeDesc getSearchRecordType(RecordTypeDesc recordType) {
        if (recordType.getSearchRecordType() != null) {
            return clientService.getBasicMetaData().getSearchRecordType(recordType.getSearchRecordType());
        }
        if (recordType.getType().equals(BasicRecordType.CUSTOM_RECORD_TYPE.getType())) {
            return clientService.getBasicMetaData().getSearchRecordType(BasicRecordType.CUSTOM_RECORD.getType());
        }
        if (recordType.getType().equals(BasicRecordType.CUSTOM_TRANSACTION_TYPE.getType())) {
            return clientService.getBasicMetaData().getSearchRecordType(BasicRecordType.TRANSACTION.getType());
        }
        return null;
    }

}
