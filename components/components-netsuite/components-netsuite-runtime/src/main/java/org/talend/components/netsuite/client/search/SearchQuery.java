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

package org.talend.components.netsuite.client.search;

import static org.talend.components.netsuite.client.model.beans.Beans.getProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.toInitialLower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.NsSearchResult;
import org.talend.components.netsuite.client.model.BasicRecordType;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.client.model.beans.PropertyInfo;
import org.talend.components.netsuite.client.model.search.SearchFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorName;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorType;
import org.talend.components.netsuite.client.model.search.SearchFieldType;

/**
 *
 */
public class SearchQuery<SearchT, RecT> {

    protected NetSuiteClientService<?> clientService;
    protected MetaDataSource metaDataSource;

    protected String recordTypeName;
    protected RecordTypeInfo recordTypeInfo;
    protected SearchRecordTypeDesc searchRecordTypeDesc;

    protected SearchT search;             // search class' instance
    protected SearchT searchBasic;        // search basic class' instance
    protected SearchT searchAdvanced;     // search advanced class' instance

    protected String savedSearchId;

    protected List<Object> customFieldList = new ArrayList<>();

    public SearchQuery(NetSuiteClientService<?> clientService, MetaDataSource metaDataSource) throws NetSuiteException {
        this.clientService = clientService;
        this.metaDataSource = metaDataSource != null ? metaDataSource : clientService.getMetaDataSource();
    }

    public SearchQuery target(final String recordTypeName) throws NetSuiteException {
        this.recordTypeName = recordTypeName;

        recordTypeInfo = metaDataSource.getRecordType(recordTypeName);
        searchRecordTypeDesc = metaDataSource.getSearchRecordType(recordTypeName);

        // search not found or not supported
        if (searchRecordTypeDesc == null) {
            throw new IllegalArgumentException("Search record type not found: " + this.recordTypeName);
        }

        return this;
    }

    public SearchQuery savedSearchId(String savedSearchId) throws NetSuiteException {
        this.savedSearchId = savedSearchId;
        return this;
    }

    public RecordTypeInfo getRecordTypeInfo() {
        initSearch();
        return recordTypeInfo;
    }

    public SearchRecordTypeDesc getSearchRecordTypeDesc() {
        initSearch();
        return searchRecordTypeDesc;
    }

    private void initSearch() throws NetSuiteException {
        if (searchBasic != null) {
            return;
        }
        try {
            // get a search class instance
            if (searchRecordTypeDesc.getSearchClass() != null) {
                search = (SearchT) searchRecordTypeDesc.getSearchClass().newInstance();
            }

            // get a advanced search class instance and set 'savedSearchId' into it
            searchAdvanced = null;
            if (savedSearchId != null && savedSearchId.length() > 0) {
                if (searchRecordTypeDesc.getSearchAdvancedClass() != null) {
                    searchAdvanced = (SearchT) searchRecordTypeDesc.getSearchAdvancedClass().newInstance();
                    setProperty(searchAdvanced, "savedSearchId", savedSearchId);
                } else {
                    throw new NetSuiteException("Advanced search not available: " + recordTypeName);
                }
            }

            // basic search class not found or supported
            if (searchRecordTypeDesc.getSearchBasicClass() == null) {
                throw new IllegalArgumentException("Search basic class not found: " + recordTypeName);
            }

            // get a basic search class instance
            searchBasic = (SearchT) searchRecordTypeDesc.getSearchBasicClass().newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
    }

    public SearchQuery condition(SearchCondition condition)
            throws NetSuiteException {

        initSearch();

        BeanInfo searchMetaData = Beans.getBeanInfo(searchRecordTypeDesc.getSearchBasicClass());

        String fieldName = toInitialLower(condition.getFieldName());
        PropertyInfo propertyInfo = searchMetaData.getProperty(fieldName);

        SearchFieldOperatorName operatorQName =
                new SearchFieldOperatorName(condition.getOperatorName());

        if (propertyInfo != null) {
            Object searchField = processConditionForSearchRecord(searchBasic, condition);
            setProperty(searchBasic, fieldName, searchField);

        } else {
            String dataType = operatorQName.getDataType();
            SearchFieldType searchFieldType = null;
            if (SearchFieldOperatorType.STRING.dataTypeEquals(dataType)) {
                searchFieldType = SearchFieldType.CUSTOM_STRING;
            } else if (SearchFieldOperatorType.BOOLEAN.dataTypeEquals(dataType)) {
                searchFieldType = SearchFieldType.CUSTOM_BOOLEAN;
            } else if (SearchFieldOperatorType.LONG.dataTypeEquals(dataType)) {
                searchFieldType = SearchFieldType.CUSTOM_LONG;
            } else if (SearchFieldOperatorType.DOUBLE.dataTypeEquals(dataType)) {
                searchFieldType = SearchFieldType.CUSTOM_DOUBLE;
            } else if (SearchFieldOperatorType.DATE.dataTypeEquals(dataType) ||
                    SearchFieldOperatorType.PREDEFINED_DATE.dataTypeEquals(dataType)) {
                searchFieldType = SearchFieldType.CUSTOM_DATE;
            } else if (SearchFieldOperatorType.MULTI_SELECT.dataTypeEquals(dataType)) {
                searchFieldType = SearchFieldType.CUSTOM_MULTI_SELECT;
            } else if (SearchFieldOperatorType.ENUM_MULTI_SELECT.dataTypeEquals(dataType)) {
                searchFieldType = SearchFieldType.CUSTOM_SELECT;
            } else {
                throw new NetSuiteException("Invalid data type: " + searchFieldType);
            }

            Object searchField = processCondition(searchFieldType, condition);
            customFieldList.add(searchField);
        }

        return this;
    }

    private Object processConditionForSearchRecord(Object searchRecord, SearchCondition condition) throws NetSuiteException {
        String fieldName = toInitialLower(condition.getFieldName());
        BeanInfo beanInfo = Beans.getBeanInfo(searchRecord.getClass());
        Class<?> searchFieldClass = beanInfo.getProperty(fieldName).getWriteType();
        SearchFieldType fieldType = SearchFieldType.getByFieldTypeName(searchFieldClass.getSimpleName());
        Object searchField = processCondition(fieldType, condition);
        return searchField;
    }

    private Object processCondition(SearchFieldType fieldType, SearchCondition condition) throws NetSuiteException {
        try {
            String searchFieldName = toInitialLower(condition.getFieldName());
            String searchOperator = condition.getOperatorName();
            List<String> searchValue = condition.getValues();

            SearchFieldAdapter<?> fieldAdapter = metaDataSource.getBasicMetaData().getSearchFieldAdapter(fieldType);
            Object searchField = fieldAdapter.populate(searchFieldName, searchOperator, searchValue);

            return searchField;
        } catch (IllegalArgumentException e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
    }

    public SearchT toNativeQuery() throws NetSuiteException {
        initSearch();

        BasicRecordType basicRecordType = BasicRecordType.getByType(searchRecordTypeDesc.getType());
        if (BasicRecordType.TRANSACTION == basicRecordType) {
            SearchFieldAdapter<?> fieldAdapter = metaDataSource.getBasicMetaData()
                    .getSearchFieldAdapter(SearchFieldType.SELECT);
            Object searchTypeField = fieldAdapter.populate(
                    "List.anyOf", Arrays.asList(recordTypeInfo.getRecordType().getType()));
            setProperty(searchBasic, "type", searchTypeField);

        } else if (BasicRecordType.CUSTOM_RECORD == basicRecordType) {
            CustomRecordTypeInfo customRecordTypeInfo = (CustomRecordTypeInfo) recordTypeInfo;
            NsRef customizationRef = customRecordTypeInfo.getRef();

            Object recType = metaDataSource.getBasicMetaData().createInstance(RefType.CUSTOMIZATION_REF.getTypeName());
            setProperty(recType, "scriptId", customizationRef.getScriptId());
            setProperty(recType, "internalId", customizationRef.getInternalId());

            setProperty(searchBasic, "recType", recType);
        }


        if (!customFieldList.isEmpty()) {
            Object customFieldListWrapper = metaDataSource.getBasicMetaData()
                    .createInstance("SearchCustomFieldList");
            List<Object> customFields = (List<Object>) getProperty(customFieldListWrapper, "customField");
            for (Object customField : customFieldList) {
                customFields.add(customField);
            }
            setProperty(searchBasic, "customFieldList", customFieldListWrapper);
        }

        SearchT searchRecord;
        if (searchRecordTypeDesc.getSearchClass() != null) {
            setProperty(search, "basic", searchBasic);
            searchRecord = search;
            if (searchAdvanced != null) {
                setProperty(searchAdvanced, "condition", search);
                searchRecord = searchAdvanced;
            }
        } else {
            searchRecord = searchBasic;
        }

        return searchRecord;
    }

    public SearchResultSet<RecT> search() throws NetSuiteException {
        Object searchRecord = toNativeQuery();
        NsSearchResult result = clientService.search(searchRecord);
        if (!result.isSuccess()) {
            NetSuiteClientService.checkError(result.getStatus());
        }
        SearchResultSet<RecT> resultSet = new SearchResultSet<>(clientService,
                recordTypeInfo.getRecordType(), searchRecordTypeDesc, result);
        return resultSet;
    }

}
