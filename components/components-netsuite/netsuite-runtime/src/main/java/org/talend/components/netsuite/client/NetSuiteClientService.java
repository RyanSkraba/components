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

import static org.talend.components.netsuite.client.model.beans.Beans.getSimpleProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.toInitialUpper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.BasicRecordType;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.client.search.SearchQuery;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;

/**
 *
 */
public abstract class NetSuiteClientService<PortT> {

    protected transient final Logger logger = LoggerFactory.getLogger(getClass());

    public static final int DEFAULT_PAGE_SIZE = 100;

    public static final String MESSAGE_LOGGING_ENABLED_PROPERTY_NAME =
            "org.talend.components.netsuite.client.messageLoggingEnabled";

    protected String endpointUrl;

    protected NetSuiteCredentials credentials;

    protected NsSearchPreferences searchPreferences;
    protected NsPreferences preferences;

    protected ReentrantLock lock = new ReentrantLock();

    protected boolean messageLoggingEnabled = false;

    protected int retryCount = 3;
    protected int retriesBeforeLogin = 2;
    protected int retryInterval = 5;

    protected int searchPageSize = DEFAULT_PAGE_SIZE;
    protected boolean bodyFieldsOnly = true;
    protected boolean returnSearchColumns = false;

    protected boolean treatWarningsAsErrors = false;
    protected boolean disableMandatoryCustomFieldValidation = false;

    protected boolean useRequestLevelCredentials = false;

    protected boolean loggedIn = false;

    protected PortT port;

    protected BasicMetaData basicMetaData;

    protected boolean customizationEnabled = true;

    protected Map<String, CustomRecordTypeInfo> customRecordTypeMap = new HashMap<>();
    protected boolean customRecordTypesLoaded = false;

    protected Map<BasicRecordType, List<Object>> customFieldMap = new HashMap<>();
    protected Map<RecordTypeDesc, Map<String, CustomFieldDesc>> recordCustomFieldMap = new HashMap<>();
    protected boolean customFieldsLoaded = false;

    protected Map<String, Map<String, CustomFieldDesc>> customRecordCustomFieldMap = new HashMap<>();

    protected static final List<BasicRecordType> fieldCustomizationTypes = Collections.unmodifiableList(Arrays.asList(
            BasicRecordType.CRM_CUSTOM_FIELD,
            BasicRecordType.ENTITY_CUSTOM_FIELD,
            BasicRecordType.ITEM_CUSTOM_FIELD,
            BasicRecordType.OTHER_CUSTOM_FIELD,
            BasicRecordType.TRANSACTION_BODY_CUSTOM_FIELD,
            BasicRecordType.TRANSACTION_COLUMN_CUSTOM_FIELD
    ));

    protected NetSuiteClientService() {
        super();

        System.setProperty("com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot", "true");
//        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public NetSuiteCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(NetSuiteCredentials credentials) {
        this.credentials = credentials;
    }

    public int getSearchPageSize() {
        return searchPageSize;
    }

    public void setSearchPageSize(int searchPageSize) {
        this.searchPageSize = searchPageSize;
    }

    public boolean isBodyFieldsOnly() {
        return bodyFieldsOnly;
    }

    public void setBodyFieldsOnly(boolean bodyFieldsOnly) {
        this.bodyFieldsOnly = bodyFieldsOnly;
    }

    public boolean isReturnSearchColumns() {
        return returnSearchColumns;
    }

    public void setReturnSearchColumns(boolean returnSearchColumns) {
        this.returnSearchColumns = returnSearchColumns;
    }

    public boolean isTreatWarningsAsErrors() {
        return treatWarningsAsErrors;
    }

    public void setTreatWarningsAsErrors(boolean treatWarningsAsErrors) {
        this.treatWarningsAsErrors = treatWarningsAsErrors;
    }

    public boolean isDisableMandatoryCustomFieldValidation() {
        return disableMandatoryCustomFieldValidation;
    }

    public void setDisableMandatoryCustomFieldValidation(boolean disableMandatoryCustomFieldValidation) {
        this.disableMandatoryCustomFieldValidation = disableMandatoryCustomFieldValidation;
    }

    public boolean isUseRequestLevelCredentials() {
        return useRequestLevelCredentials;
    }

    public void setUseRequestLevelCredentials(boolean useRequestLevelCredentials) {
        this.useRequestLevelCredentials = useRequestLevelCredentials;
    }

    public boolean isCustomizationEnabled() {
        return customizationEnabled;
    }

    public void setCustomizationEnabled(boolean customizationEnabled) {
        this.customizationEnabled = customizationEnabled;
    }

    public void login() throws NetSuiteException {
        lock.lock();
        try {
            relogin();
        } finally {
            lock.unlock();
        }
    }

    public SearchQuery newSearch() throws NetSuiteException {
        return new SearchQuery(this);
    }

    public abstract <RecT, SearchT> NsSearchResult<RecT> search(final SearchT searchRecord)
            throws NetSuiteException;

    public abstract <RecT> NsSearchResult<RecT> searchMore(final int pageIndex)
            throws NetSuiteException;

    public abstract <RecT> NsSearchResult<RecT> searchMoreWithId(final String searchId, final int pageIndex)
            throws NetSuiteException;

    public abstract <RecT> NsSearchResult<RecT> searchNext()
            throws NetSuiteException;

    public abstract <RecT, RefT> NsWriteResponse<RefT> add(final RecT record)
            throws NetSuiteException;

    public abstract <RecT, RefT> List<NsWriteResponse<RefT>> addList(final List<RecT> records)
            throws NetSuiteException;

    public abstract <RecT, RefT> NsWriteResponse<RefT> update(final RecT record)
            throws NetSuiteException;

    public abstract <RecT, RefT> List<NsWriteResponse<RefT>> updateList(final List<RecT> records)
            throws NetSuiteException;

    public abstract <RecT, RefT> NsWriteResponse<RefT> upsert(final RecT record)
            throws NetSuiteException;

    public abstract <RecT, RefT> List<NsWriteResponse<RefT>> upsertList(final List<RecT> records)
            throws NetSuiteException;

    public abstract <RefT> NsWriteResponse<RefT> delete(final RefT ref)
            throws NetSuiteException;

    public abstract <RefT> List<NsWriteResponse<RefT>> deleteList(final List<RefT> refs)
            throws NetSuiteException;

    public <R> R execute(PortOperation<R, PortT> op) throws NetSuiteException {
        if (useRequestLevelCredentials) {
            return executeUsingRequestLevelCredentials(op);
        } else {
            return executeUsingLogin(op);
        }
    }

    public BasicMetaData getBasicMetaData() {
        return basicMetaData;
    }

    public Collection<RecordTypeInfo> getRecordTypes() {
        List<RecordTypeInfo> recordTypes = new ArrayList<>();

        Collection<RecordTypeDesc> standardRecordTypes = basicMetaData.getRecordTypes();
        for (RecordTypeDesc recordType : standardRecordTypes) {
            recordTypes.add(new RecordTypeInfo(recordType));
        }

        retrieveCustomRecordTypes();

        for (RecordTypeInfo recordTypeInfo : customRecordTypeMap.values()) {
            recordTypes.add(recordTypeInfo);
        }

        return recordTypes;
    }

    public Collection<NamedThing> getSearchableTypes() throws NetSuiteException {
        List<NamedThing> searchableTypes = new ArrayList<>(256);

        Collection<RecordTypeInfo> recordTypes = getRecordTypes();

        for (RecordTypeInfo recordTypeInfo : recordTypes) {
            RecordTypeDesc recordTypeDesc = recordTypeInfo.getRecordType();
            if (recordTypeDesc.getSearchRecordType() != null) {
                SearchRecordTypeDesc searchRecordType = basicMetaData.getSearchRecordType(recordTypeDesc);
                if (searchRecordType != null) {
                    searchableTypes.add(new SimpleNamedThing(recordTypeInfo.getName(), recordTypeInfo.getDisplayName()));
                }
            }
        }

        return searchableTypes;
    }

    public TypeDesc getTypeInfo(final Class<?> clazz) {
        return getTypeInfo(clazz.getSimpleName());
    }

    public TypeDesc getTypeInfo(final String typeName) {
        TypeDesc baseTypeDesc;
        String targetTypeName = null;
        Class<?> targetTypeClass;
        List<FieldDesc> baseFieldDescList;

        RecordTypeInfo recordTypeInfo = getRecordType(typeName);
        if (recordTypeInfo != null) {
            if (recordTypeInfo instanceof CustomRecordTypeInfo) {
                CustomRecordTypeInfo customRecordTypeInfo = (CustomRecordTypeInfo) recordTypeInfo;
                baseTypeDesc = basicMetaData.getTypeInfo(customRecordTypeInfo.getRecordType().getTypeName());
                targetTypeName = customRecordTypeInfo.getName();
            } else {
                baseTypeDesc = basicMetaData.getTypeInfo(typeName);
            }
        } else {
            baseTypeDesc = basicMetaData.getTypeInfo(typeName);
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
            if (fieldName.equals("CustomFieldList") || fieldName.equals("NullFieldList")) {
                continue;
            }
            resultFieldDescList.add(fieldDesc);
        }

        if (recordTypeInfo != null) {
            if (customizationEnabled) {
                // Add custom fields
                Map<String, CustomFieldDesc> customFieldMap = getRecordCustomFields(recordTypeInfo);
                for (CustomFieldDesc fieldInfo : customFieldMap.values()) {
                    resultFieldDescList.add(fieldInfo);
                }
            }
        }

        return new TypeDesc(targetTypeName, targetTypeClass, resultFieldDescList);
    }

    public RecordTypeInfo getRecordType(String typeName) {
        RecordTypeDesc recordType = basicMetaData.getRecordType(typeName);
        if (recordType != null) {
            return new RecordTypeInfo(recordType);
        }

        if (customizationEnabled) {
            retrieveCustomRecordTypes();
        }

        RecordTypeInfo recordTypeInfo = customRecordTypeMap.get(typeName);
        return recordTypeInfo;
    }

    public SearchRecordTypeDesc getSearchRecordType(String recordTypeName) {
        SearchRecordTypeDesc searchRecordType = basicMetaData.getSearchRecordType(recordTypeName);
        if (searchRecordType != null) {
            return searchRecordType;
        }
        RecordTypeInfo recordTypeInfo = getRecordType(recordTypeName);
        if (recordTypeInfo != null) {
            return getSearchRecordType(recordTypeInfo.getRecordType());
        }
        return null;
    }

    public SearchRecordTypeDesc getSearchRecordType(RecordTypeDesc recordType) {
        if (recordType.getSearchRecordType() != null) {
            return basicMetaData.getSearchRecordType(recordType.getSearchRecordType());
        }
        if (recordType.getType().equals(BasicRecordType.CUSTOM_RECORD_TYPE.getType())) {
            return basicMetaData.getSearchRecordType(BasicRecordType.CUSTOM_RECORD.getType());
        }
        if (recordType.getType().equals(BasicRecordType.CUSTOM_TRANSACTION_TYPE.getType())) {
            return basicMetaData.getSearchRecordType(BasicRecordType.TRANSACTION.getType());
        }
        return null;
    }

    protected Map<String, CustomFieldDesc> getRecordCustomFields(RecordTypeInfo recordTypeInfo) throws NetSuiteException {
        lock.lock();
        try {
            RecordTypeDesc recordType = recordTypeInfo.getRecordType();

            Map<String, CustomFieldDesc> recordCustomFields;

            if (recordTypeInfo instanceof CustomRecordTypeInfo) {
                retrieveCustomRecordCustomFields((CustomRecordTypeInfo) recordTypeInfo);

                recordCustomFields = customRecordCustomFieldMap.get(recordTypeInfo.getName());

            } else {
                retrieveCustomFields();

                recordCustomFields = recordCustomFieldMap.get(recordType.getType());

                if (recordCustomFields == null) {
                    recordCustomFields = new HashMap<>();

                    for (BasicRecordType customizationType : fieldCustomizationTypes) {
                        List<Object> customFieldList = customFieldMap.get(customizationType);

                        Map<String, CustomFieldDesc> customFieldDescMap =
                                createCustomFieldDescMap(recordType, customizationType, customFieldList);
                        recordCustomFields.putAll(customFieldDescMap);
                    }

                    recordCustomFieldMap.put(recordType, recordCustomFields);
                }
            }

            return recordCustomFields;

        } finally {
            lock.unlock();
        }
    }

    protected <T> Map<String, CustomFieldDesc> createCustomFieldDescMap(
            RecordTypeDesc recordType, BasicRecordType customizationType, List<T> customFieldList) throws NetSuiteException {

        Map<String, CustomFieldDesc> customFieldDescMap = new HashMap<>();

        for (T customField : customFieldList) {

            CustomFieldRefType customFieldRefType = basicMetaData
                    .getCustomFieldRefType(recordType.getType(), customizationType, customField);

            if (customFieldRefType != null) {
                CustomFieldDesc customFieldInfo = new CustomFieldDesc();

                String internalId = (String) getSimpleProperty(customField, "internalId");
                String scriptId = (String) getSimpleProperty(customField, "scriptId");
                String label = (String) getSimpleProperty(customField, "label");

                NsRef customizationRef = new NsRef();
                customizationRef.setRefType(RefType.CUSTOMIZATION_REF);
                customizationRef.setType(customizationType.getType());
                customizationRef.setName(label);
                customizationRef.setInternalId(internalId);
                customizationRef.setScriptId(scriptId);

                customFieldInfo.setRef(customizationRef);
                customFieldInfo.setName(customizationRef.getScriptId());
                customFieldInfo.setCustomFieldType(customFieldRefType);

                TypeDesc typeDesc = basicMetaData.getTypeInfo(customFieldRefType.getTypeName());
                BeanInfo beanInfo = Beans.getBeanInfo(typeDesc.getTypeClass());
                Class<?> valueType = beanInfo.getProperty("value").getWriteType();
                customFieldInfo.setValueType(valueType);

                customFieldInfo.setNullable(true);

                customFieldDescMap.put(customFieldInfo.getName(), customFieldInfo);
            }
        }

        return customFieldDescMap;
    }

    protected void retrieveCustomRecordTypes() throws NetSuiteException {
        try {
            lock.lock();

            if (!customRecordTypesLoaded) {
                List<NsRef> customTypes = new ArrayList<>();

                List<NsRef> customRecordTypes = retrieveCustomizationIds(
                        BasicRecordType.CUSTOM_RECORD_TYPE);
                customTypes.addAll(customRecordTypes);

                List<NsRef> customTransactionTypes = retrieveCustomizationIds(
                        BasicRecordType.CUSTOM_TRANSACTION_TYPE);
                customTypes.addAll(customTransactionTypes);

                for (NsRef customizationRef : customTypes) {
                    String recordType = customizationRef.getType();
                    RecordTypeDesc recordTypeDesc = null;
                    BasicRecordType basicRecordType = BasicRecordType.getByType(recordType);
                    if (basicRecordType != null) {
                        recordTypeDesc = basicMetaData.getRecordType(toInitialUpper(basicRecordType.getSearchType()));
                    }

                    CustomRecordTypeInfo customRecordTypeInfo = new CustomRecordTypeInfo(
                            customizationRef.getScriptId(), recordTypeDesc, customizationRef);
                    customRecordTypeMap.put(customRecordTypeInfo.getName(), customRecordTypeInfo);
                }

                customRecordTypesLoaded = true;
            }
        } finally {
            lock.unlock();
        }
    }

    protected void retrieveCustomFields() throws NetSuiteException {
        lock.lock();
        try {
            if (!customFieldsLoaded) {
                Map<BasicRecordType, List<NsRef>> fieldCustomizationRefs = new HashMap<>(32);
                for (BasicRecordType customizationType : fieldCustomizationTypes) {
                    List<NsRef> customizationRefs = retrieveCustomizationIds(customizationType);
                    fieldCustomizationRefs.put(customizationType, customizationRefs);
                }

                for (BasicRecordType customizationType : fieldCustomizationTypes) {
                    List<NsRef> customizationRefs = fieldCustomizationRefs.get(customizationType);
                    List<Object> fieldCustomizationList = retrieveCustomizations(customizationRefs);
                    customFieldMap.put(customizationType, fieldCustomizationList);
                }

                customFieldsLoaded = true;
            }
        } finally {
            lock.unlock();
        }
    }

    protected void retrieveCustomRecordCustomFields(CustomRecordTypeInfo recordTypeInfo) throws NetSuiteException {
        lock.lock();
        try {
            Map<String, CustomFieldDesc> recordCustomFieldMap = customRecordCustomFieldMap.get(recordTypeInfo.getName());

            if (recordCustomFieldMap != null) {
                return;
            }

            recordCustomFieldMap = retrieveCustomRecordCustomFields(
                    recordTypeInfo.getRecordType(), recordTypeInfo.getRef());

            customRecordCustomFieldMap.put(recordTypeInfo.getName(), recordCustomFieldMap);

        } finally {
            lock.unlock();
        }
    }

    protected abstract List<NsRef> retrieveCustomizationIds(final BasicRecordType type) throws NetSuiteException;

    protected abstract <T> List<T> retrieveCustomizations(final List<NsRef> nsCustomizationRefs)
            throws NetSuiteException;

    protected abstract Map<String, CustomFieldDesc> retrieveCustomRecordCustomFields(
            final RecordTypeDesc recordType, final NsRef nsCustomizationRef) throws NetSuiteException;

    protected <R> R executeUsingLogin(PortOperation<R, PortT> op) throws NetSuiteException {
        lock.lock();
        try {
            login(false);

            try {
                return op.execute(port);
            } catch (Exception e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        } finally {
            lock.unlock();
        }
    }

    private <R> R executeUsingRequestLevelCredentials(PortOperation<R, PortT> op) throws NetSuiteException {
        lock.lock();
        try {
            relogin();

            try {
                return op.execute(port);
            } catch (Exception e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        } finally {
            lock.unlock();
        }
    }

    protected void setHeader(PortT port, Header header) {
        BindingProvider provider = (BindingProvider) port;
        Map<String, Object> requestContext = provider.getRequestContext();
        List<Header> list = (List<Header>) requestContext.get(Header.HEADER_LIST);
        if (list == null) {
            list = new ArrayList<>();
            requestContext.put(Header.HEADER_LIST, list);
        }
        removeHeader(list, header.getName());
        list.add(header);
    }

    protected void removeHeader(QName name) {
        removeHeader(port, name);
    }

    protected void removeHeader(PortT port, QName name) {
        BindingProvider provider = (BindingProvider) port;
        Map<String, Object> requestContext = provider.getRequestContext();
        List<Header> list = (List<Header>) requestContext.get(Header.HEADER_LIST);
        removeHeader(list, name);
    }

    private void removeHeader(List<Header> list, QName name) {
        if (list != null) {
            Iterator<Header> headerIterator = list.iterator();
            while (headerIterator.hasNext()) {
                Header header = headerIterator.next();
                if (header.getName().equals(name)) {
                    headerIterator.remove();
                }
            }
        }
    }

    private void relogin() throws NetSuiteException {
        login(true);
    }

    private void login(boolean relogin) throws NetSuiteException {
        if (relogin) {
            loggedIn = false;
        }
        if (loggedIn) {
            return;
        }

        if (port != null) {
            try {
                doLogout();
            } catch (Exception e) {
            }
        }

        doLogin();

        NsSearchPreferences searchPreferences = new NsSearchPreferences();
        searchPreferences.setPageSize(searchPageSize);
        searchPreferences.setBodyFieldsOnly(Boolean.valueOf(bodyFieldsOnly));
        searchPreferences.setReturnSearchColumns(Boolean.valueOf(returnSearchColumns));

        this.searchPreferences = searchPreferences;

        NsPreferences preferences = new NsPreferences();
        preferences.setDisableMandatoryCustomFieldValidation(disableMandatoryCustomFieldValidation);
        preferences.setWarningAsError(treatWarningsAsErrors);

        this.preferences = preferences;

        setPreferences(port, preferences, searchPreferences);

        loggedIn = true;
    }

    protected abstract void doLogout() throws NetSuiteException;

    protected abstract void doLogin() throws NetSuiteException;

    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets the number of retry attempts made when an operation fails.
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * Sets the length of time (in seconds) that a session will sleep before attempting the retry of a failed operation.
     */
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public boolean isMessageLoggingEnabled() {
        return messageLoggingEnabled;
    }

    public void setMessageLoggingEnabled(boolean messageLoggingEnabled) {
        this.messageLoggingEnabled = messageLoggingEnabled;
    }


    protected void waitForRetryInterval() {
        try {
            Thread.sleep(getRetryInterval() * 1000);
        } catch (InterruptedException e) {

        }
    }

    protected void setPreferences(PortT port,
            NsPreferences nsPreferences, NsSearchPreferences nsSearchPreferences) throws NetSuiteException {

        Object searchPreferences = createNativeSearchPreferences(nsSearchPreferences);
        Object preferences = createNativePreferences(nsPreferences);
        try {
            Header searchPreferencesHeader = new Header(
                    new QName(getPlatformMessageNamespaceUri(), "searchPreferences"),
                    searchPreferences, new JAXBDataBinding(searchPreferences.getClass()));

            Header preferencesHeader = new Header(
                    new QName(getPlatformMessageNamespaceUri(), "preferences"),
                    preferences, new JAXBDataBinding(preferences.getClass()));

            setHeader(port, preferencesHeader);
            setHeader(port, searchPreferencesHeader);

        } catch (JAXBException e) {
            throw new NetSuiteException("XML binding error", e);
        }
    }

    protected void setLoginHeaders(PortT port) throws NetSuiteException {
        if (!StringUtils.isEmpty(credentials.getApplicationId())) {
            Object applicationInfo = createNativeApplicationInfo(credentials);
            try {
                if (applicationInfo != null) {
                    Header appInfoHeader = new Header(
                            new QName(getPlatformMessageNamespaceUri(), "applicationInfo"),
                            applicationInfo, new JAXBDataBinding(applicationInfo.getClass()));
                    setHeader(port, appInfoHeader);
                }
            } catch (JAXBException e) {
                throw new NetSuiteException("XML binding error", e);
            }
        }
    }

    protected void remoteLoginHeaders(PortT port) throws NetSuiteException {
        removeHeader(port, new QName(getPlatformMessageNamespaceUri(), "applicationInfo"));
    }

    protected abstract String getPlatformMessageNamespaceUri();

    protected abstract <T> T createNativePreferences(NsPreferences nsPreferences);

    protected abstract <T> T createNativeSearchPreferences(NsSearchPreferences nsSearchPreferences);

    protected abstract <T> T createNativeApplicationInfo(NetSuiteCredentials nsCredentials);

    protected abstract <T> T createNativePassport(NetSuiteCredentials nsCredentials);

    protected abstract PortT getNetSuitePort(String defaultEndpointUrl, String account) throws NetSuiteException;

    public interface PortOperation<R, PortT> {
        R execute(PortT port) throws Exception;
    }
}
