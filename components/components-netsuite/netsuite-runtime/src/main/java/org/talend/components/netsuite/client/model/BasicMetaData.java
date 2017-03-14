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

package org.talend.components.netsuite.client.model;

import static org.talend.components.netsuite.client.model.TypeUtils.collectXmlTypes;
import static org.talend.components.netsuite.client.model.beans.Beans.toInitialUpper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.client.model.beans.PropertyInfo;
import org.talend.components.netsuite.client.model.customfield.CrmCustomFieldAdapter;
import org.talend.components.netsuite.client.model.customfield.CustomFieldAdapter;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.client.model.customfield.DefaultCustomFieldAdapter;
import org.talend.components.netsuite.client.model.customfield.EntityCustomFieldAdapter;
import org.talend.components.netsuite.client.model.customfield.ItemCustomFieldAdapter;
import org.talend.components.netsuite.client.model.customfield.ItemOptionCustomFieldAdapter;
import org.talend.components.netsuite.client.model.customfield.TransactionBodyCustomFieldAdapter;
import org.talend.components.netsuite.client.model.customfield.TransactionColumnCustomFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchBooleanFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchDateFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchDoubleFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchEnumMultiSelectFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorName;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorType;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorTypeDesc;
import org.talend.components.netsuite.client.model.search.SearchFieldType;
import org.talend.components.netsuite.client.model.search.SearchLongFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchMultiSelectFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchStringFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchTextNumberFieldAdapter;

/**
 *
 */
public abstract class BasicMetaData {
    protected transient final Logger logger = LoggerFactory.getLogger(getClass());

    protected Map<String, Class<?>> typeMap = new HashMap<>();

    protected Map<String, Class<?>> searchFieldMap = new HashMap<>();
    protected Map<SearchFieldOperatorType, SearchFieldOperatorTypeDesc> searchFieldOperatorTypeMap = new HashMap<>();
    protected Map<SearchFieldType, SearchFieldAdapter<?>> searchFieldAdapterMap = new HashMap<>();

    protected Map<BasicRecordType, CustomFieldAdapter<?>> customFieldAdapterMap = new HashMap<>();

    protected BasicMetaData() {
        bindCustomFieldAdapters();
    }

    protected void bindTypeHierarchy(Class<?> baseClass) {
        Set<Class<?>> classes = new HashSet<>();
        collectXmlTypes(baseClass, baseClass, classes);
        for (Class<?> clazz : classes) {
            bindType(clazz, null);
        }
    }

    protected void bindType(Class<?> typeClass, String typeName) {
        String typeNameToRegister = typeName != null ? typeName : typeClass.getSimpleName();
        if (typeMap.containsKey(typeNameToRegister)) {
            Class<?> clazz = typeMap.get(typeNameToRegister);
            if (clazz == typeClass) {
                return;
            } else {
                throw new IllegalArgumentException("Type already registered: " +
                        typeNameToRegister + ", class to register is " +
                        typeClass + ", registered class is " +
                        typeMap.get(typeNameToRegister));
            }
        }
        typeMap.put(typeNameToRegister, typeClass);
    }

    protected void bindSearchFields(Collection<Class<?>> searchFieldClasses) {
        for (Class<?> entry : searchFieldClasses) {
            String searchFieldTypeName = entry.getSimpleName();

            searchFieldMap.put(searchFieldTypeName, entry);

            bindSearchFieldAdapter(searchFieldTypeName);
        }
    }

    protected void bindSearchFieldOperatorTypes(
            Collection<SearchFieldOperatorTypeDesc> searchFieldOperatorTypes) {

        Collection<SearchFieldOperatorTypeDesc> searchFieldOperatorTypeList = new ArrayList<>();

        for (SearchFieldOperatorTypeDesc operatorTypeDesc : searchFieldOperatorTypes) {
            searchFieldOperatorTypeList.add(operatorTypeDesc);
        }

        searchFieldOperatorTypeList.add(
                // Boolean (Synthetic)
                new SearchFieldOperatorTypeDesc(SearchFieldOperatorType.BOOLEAN,
                        SearchFieldOperatorType.SearchBooleanFieldOperator.class, null, null)
        );

        for (SearchFieldOperatorTypeDesc info : searchFieldOperatorTypeList) {
            searchFieldOperatorTypeMap.put(info.getOperatorType(), info);
        }
    }

    protected void bindSearchFieldAdapter(String fieldType) {
        SearchFieldType searchFieldType = SearchFieldType.getByFieldTypeName(fieldType);
        bindSearchFieldAdapter(searchFieldType);
    }

    protected void bindSearchFieldAdapter(final SearchFieldType searchFieldType) {
        Class<?> fieldClass = getSearchFieldClass(searchFieldType.getFieldTypeName());
        SearchFieldAdapter<?> fieldAdapter;
        switch (searchFieldType) {
        case BOOLEAN:
        case CUSTOM_BOOLEAN:
            fieldAdapter = new SearchBooleanFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        case STRING:
        case CUSTOM_STRING:
            fieldAdapter = new SearchStringFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        case TEXT_NUMBER:
            fieldAdapter = new SearchTextNumberFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        case LONG:
        case CUSTOM_LONG:
            fieldAdapter = new SearchLongFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        case DOUBLE:
        case CUSTOM_DOUBLE:
            fieldAdapter = new SearchDoubleFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        case DATE:
        case CUSTOM_DATE:
            fieldAdapter = new SearchDateFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        case MULTI_SELECT:
        case CUSTOM_MULTI_SELECT:
            fieldAdapter = new SearchMultiSelectFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        case SELECT:
        case CUSTOM_SELECT:
            fieldAdapter = new SearchEnumMultiSelectFieldAdapter<>(this, searchFieldType, fieldClass);
            break;
        default:
            throw new IllegalArgumentException("Invalid search field type: " + searchFieldType);
        }
        searchFieldAdapterMap.put(searchFieldType, fieldAdapter);
    }

    protected void bindCustomFieldAdapters() {
        bindCustomFieldAdapter(new CrmCustomFieldAdapter<>());
        bindCustomFieldAdapter(new EntityCustomFieldAdapter<>());
        bindCustomFieldAdapter(new ItemCustomFieldAdapter<>());
        bindCustomFieldAdapter(new ItemOptionCustomFieldAdapter<>());
        bindCustomFieldAdapter(new TransactionBodyCustomFieldAdapter<>());
        bindCustomFieldAdapter(new TransactionColumnCustomFieldAdapter<>());
        bindCustomFieldAdapter(new DefaultCustomFieldAdapter<>(BasicRecordType.CUSTOM_LIST, false));
        bindCustomFieldAdapter(new DefaultCustomFieldAdapter<>(BasicRecordType.CUSTOM_RECORD, true));
        bindCustomFieldAdapter(new DefaultCustomFieldAdapter<>(BasicRecordType.CUSTOM_RECORD_TYPE, true));
        bindCustomFieldAdapter(new DefaultCustomFieldAdapter<>(BasicRecordType.CUSTOM_TRANSACTION_TYPE, true));
        bindCustomFieldAdapter(new DefaultCustomFieldAdapter<>(BasicRecordType.OTHER_CUSTOM_FIELD, false));
        bindCustomFieldAdapter(new DefaultCustomFieldAdapter<>(BasicRecordType.ITEM_NUMBER_CUSTOM_FIELD, false));
    }

    protected void bindCustomFieldAdapter(CustomFieldAdapter<?> adapter) {
        customFieldAdapterMap.put(adapter.getType(), adapter);
    }

    protected Class<?> getTypeClass(String typeName) {
        Class<?> clazz = typeMap.get(typeName);
        if (clazz != null) {
            return clazz;
        }
        RecordTypeDesc recordType = getRecordType(typeName);
        if (recordType != null) {
            return recordType.getRecordClass();
        }
        return null;
    }

    public TypeDesc getTypeInfo(String typeName) {
        Class<?> clazz = getTypeClass(typeName);
        return clazz != null ? getTypeInfo(clazz) : null;
    }

    public TypeDesc getTypeInfo(Class<?> clazz) {
        BeanInfo beanInfo = Beans.getBeanInfo(clazz);
        List<PropertyInfo> propertyInfos = beanInfo.getProperties();

        List<FieldDesc> fields = new ArrayList<>(propertyInfos.size());

        for (PropertyInfo propertyInfo : propertyInfos) {
            String fieldName = toInitialUpper(propertyInfo.getName());

            Class fieldValueType = propertyInfo.getReadType();
            if ((propertyInfo.getName().equals("class") && fieldValueType == Class.class)) {
                continue;
            }

            boolean isKey = isKeyField(clazz, propertyInfo);
            SimpleFieldDesc fieldDesc = new SimpleFieldDesc(fieldName, fieldValueType, isKey, true);
            fieldDesc.setPropertyName(propertyInfo.getName());
            fields.add(fieldDesc);
        }

        return new TypeDesc(clazz.getSimpleName(), clazz, fields);
    }

    public abstract RecordTypeDesc getRecordType(String typeName);

    public abstract Collection<RecordTypeDesc> getRecordTypes();

    public abstract SearchRecordTypeDesc getSearchRecordType(String searchRecordType);

    public SearchRecordTypeDesc getSearchRecordType(RecordTypeDesc recordType) {
        SearchRecordTypeDesc searchRecordType = getSearchRecordType(recordType.getSearchRecordType());
        return searchRecordType;
    }

    public Class<?> getSearchFieldClass(String searchFieldType) {
        return searchFieldMap.get(searchFieldType);
    }

    public Object getSearchFieldOperatorByName(String searchFieldTypeName, String operatorName) {
        SearchFieldType fieldType = SearchFieldType.getByFieldTypeName(searchFieldTypeName);
        return getSearchFieldOperator(fieldType, operatorName);
    }

    public Object getSearchFieldOperator(SearchFieldType fieldType, String operatorName) {
        SearchFieldOperatorName operatorQName = new SearchFieldOperatorName(operatorName);
        SearchFieldOperatorType operatorType = SearchFieldType.getOperatorType(fieldType);
        if (operatorType != null) {
            SearchFieldOperatorTypeDesc def = searchFieldOperatorTypeMap.get(operatorType);
            return def.getOperator(operatorName);
        }
        for (SearchFieldOperatorTypeDesc def : searchFieldOperatorTypeMap.values()) {
            if (def.hasOperator(operatorQName)) {
                return def.getOperator(operatorName);
            }
        }
        throw new IllegalArgumentException("Unknown search field operator: " + fieldType + ", " + operatorName);
    }

    public Collection<SearchFieldOperatorName> getSearchOperatorNames() {
        Set<SearchFieldOperatorName> names = new HashSet<>();
        for (SearchFieldOperatorTypeDesc info : searchFieldOperatorTypeMap.values()) {
            names.addAll(info.getOperatorNames());
        }
        return Collections.unmodifiableSet(names);
    }

    public SearchFieldAdapter<?> getSearchFieldAdapter(SearchFieldType fieldType) {
        return searchFieldAdapterMap.get(fieldType);
    }

    public CustomFieldRefType getCustomFieldRefType(String recordType, BasicRecordType customFieldType, Object customField) {
        CustomFieldAdapter customFieldAdapter = customFieldAdapterMap.get(customFieldType);
        if (customFieldAdapter.appliesTo(recordType, customField)) {
            return customFieldAdapter.apply(customField);
        }
        return null;
    }

    protected boolean isKeyField(Class<?> entityClass, PropertyInfo propertyInfo) {
        if (propertyInfo.getName().equals("internalId")
                || propertyInfo.getName().equals("externalId")
                || propertyInfo.getName().equals("scriptId")) {
            return true;
        }
        return false;
    }

    public <T> T createInstance(String typeName) {
        Class<?> clazz = getTypeClass(typeName);
        if (clazz == null) {
            throw new NetSuiteException("Unknown type: " + typeName);
        }
        return (T) TypeUtils.createInstance(clazz);
    }

}
