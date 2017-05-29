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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.netsuite.NetSuiteErrorCode;
import org.talend.components.netsuite.NetSuiteRuntimeI18n;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.search.SearchQuery;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.java8.Function;

/**
 * NetSuiteClientService provides access to remote NetSuite endpoint.
 */
public abstract class NetSuiteClientService<PortT> {

    protected transient final Logger logger = LoggerFactory.getLogger(getClass());

    public static final long DEFAULT_CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(60);

    public static final long DEFAULT_RECEIVE_TIMEOUT = TimeUnit.SECONDS.toMillis(180);

    public static final int DEFAULT_SEARCH_PAGE_SIZE = 100;

    public static final String MESSAGE_LOGGING_ENABLED_PROPERTY_NAME =
            "org.talend.components.netsuite.client.messageLoggingEnabled";

    protected String endpointUrl;

    protected NetSuiteCredentials credentials;

    protected NsSearchPreferences searchPreferences;

    protected NsPreferences preferences;

    /** Used for synchronization of access to NetSuite port. */
    protected ReentrantLock lock = new ReentrantLock();

    /** Specifies whether logging of SOAP messages is enabled. Intended for test/debug purposes. */
    protected boolean messageLoggingEnabled = false;

    /** Web Service connection timeout, in milliseconds. */
    protected long connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    /** Web Service response receiving timeout, in milliseconds. */
    protected long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    /** Number of retries for an operation. */
    protected int retryCount = 3;

    /** Number of retries before (re-)login. */
    protected int retriesBeforeLogin = 2;

    /** Interval between retries. */
    protected int retryInterval = 5;

    /** Size of search result page. */
    protected int searchPageSize = DEFAULT_SEARCH_PAGE_SIZE;

    /** Specifies whether to return record body fields only. */
    protected boolean bodyFieldsOnly = true;

    /** Specifies whether to return search columns. */
    protected boolean returnSearchColumns = false;

    /** Specifies whether to treat warnings as errors. */
    protected boolean treatWarningsAsErrors = false;

    /** Specifies whether to disable validation for mandatory custom fields. */
    protected boolean disableMandatoryCustomFieldValidation = false;

    /** Specifies whether to use request level credentials. */
    protected boolean useRequestLevelCredentials = false;

    /** Flag indicating whether the client is logged in. */
    protected boolean loggedIn = false;

    /** NetSuite Web Service port implementor. */
    protected PortT port;

    /** Source of meta data. */
    protected MetaDataSource metaDataSource;

    protected NetSuiteClientService() {
        super();

        // Disable eager initialization of JAXBContext
        System.setProperty("com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot", "true");
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

    /**
     * Log in to NetSuite.
     *
     * @throws NetSuiteException if an error occurs during logging in
     */
    public void login() throws NetSuiteException {
        lock.lock();
        try {
            relogin();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Create new search query object.
     *
     * @return search query object
     */
    public SearchQuery newSearch() {
        return newSearch(getMetaDataSource());
    }

    /**
     * Create new search query object using given meta data source.
     *
     * @param metaDataSource meta data source
     * @return search query object
     */
    public SearchQuery newSearch(MetaDataSource metaDataSource) {
        return new SearchQuery(this, metaDataSource);
    }

    /**
     * Search records.
     *
     * <p>Retrieval of search results uses pagination. To retrieve next page use
     * {@link #searchMoreWithId(String, int)} method.
     *
     * @param searchRecord search record to be sent to NetSuite
     * @param <RecT> type of record data object
     * @param <SearchT> type of search record data object
     * @return search result wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, SearchT> NsSearchResult<RecT> search(final SearchT searchRecord)
            throws NetSuiteException;

    /**
     * Retrieve search results page by index.
     *
     * @param pageIndex page index
     * @param <RecT> type of record data object
     * @return search result wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT> NsSearchResult<RecT> searchMore(final int pageIndex)
            throws NetSuiteException;

    /**
     * Retrieve search results page by search ID and page index.
     *
     * @param searchId identifier of search
     * @param pageIndex page index
     * @param <RecT> type of record data object
     * @return search result wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT> NsSearchResult<RecT> searchMoreWithId(final String searchId, final int pageIndex)
            throws NetSuiteException;

    /**
     * Retrieve next search results page.
     *
     * @param <RecT> type of record data object
     * @return search result wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT> NsSearchResult<RecT> searchNext()
            throws NetSuiteException;

    /**
     * Retrieve a record by record ref.
     *
     * @param ref record ref data object
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return read response wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> NsReadResponse<RecT> get(final RefT ref)
            throws NetSuiteException;

    /**
     * Retrieve records by record refs.
     *
     * @param refs list of record refs
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return list of read response wrapper objects
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> List<NsReadResponse<RecT>> getList(final List<RefT> refs)
            throws NetSuiteException;

    /**
     * Add a record.
     *
     * @param record record data object to be sent to NetSuite
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return write response wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> NsWriteResponse<RefT> add(final RecT record)
            throws NetSuiteException;

    /**
     * Add records.
     *
     * @param records list of record data objects to be sent to NetSuite
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return list of write response wrapper objects
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> List<NsWriteResponse<RefT>> addList(final List<RecT> records)
            throws NetSuiteException;

    /**
     * Update a record.
     *
     * @param record record data object to be sent to NetSuite
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return write response wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> NsWriteResponse<RefT> update(final RecT record)
            throws NetSuiteException;

    /**
     * Update records.
     *
     * @param records list of record data objects to be sent to NetSuite
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return list of write response wrapper objects
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> List<NsWriteResponse<RefT>> updateList(final List<RecT> records)
            throws NetSuiteException;

    /**
     * Upsert a record.
     *
     * @param record record data object to be sent to NetSuite
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return write response wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> NsWriteResponse<RefT> upsert(final RecT record)
            throws NetSuiteException;

    /**
     * Upsert records.
     *
     * @param records list of record data objects to be sent to NetSuite
     * @param <RecT> type of record data object
     * @param <RefT> type of record ref data object
     * @return list of write response wrapper objects
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RecT, RefT> List<NsWriteResponse<RefT>> upsertList(final List<RecT> records)
            throws NetSuiteException;

    /**
     * Delete a record.
     *
     * @param ref record ref data object to be sent to NetSuite
     * @param <RefT> type of record ref data object
     * @return write response wrapper object
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RefT> NsWriteResponse<RefT> delete(final RefT ref)
            throws NetSuiteException;

    /**
     * Delete records.
     *
     * @param refs list of record ref data objects to be sent to NetSuite
     * @param <RefT> type of record ref data object
     * @return list of write response wrapper objects
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public abstract <RefT> List<NsWriteResponse<RefT>> deleteList(final List<RefT> refs)
            throws NetSuiteException;

    /**
     * Execute an operation that use NetSuite web service port.
     *
     * @param op operation to be executed
     * @param <R> type of operation result
     * @return result of operation
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    public <R> R execute(PortOperation<R, PortT> op) throws NetSuiteException {
        if (useRequestLevelCredentials) {
            return executeUsingRequestLevelCredentials(op);
        } else {
            return executeUsingLogin(op);
        }
    }

    /**
     * Execute an operation within client lock.
     *
     * @param func operation to be executed
     * @param param parameter object
     * @param <T> type of parameter
     * @param <R> type of result
     * @return result of execution
     */
    public <T, R> R executeWithLock(Function<T, R> func, T param) {
        lock.lock();
        try {
            return func.apply(param);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get basic meta data used by this client.
     *
     * @return basic meta data
     */
    public abstract BasicMetaData getBasicMetaData();

    /**
     * Get meta data source used by this client.
     *
     * @return meta data source
     */
    public MetaDataSource getMetaDataSource() {
        return metaDataSource;
    }

    /**
     * Create new instance of default meta data source.
     *
     * @return meta data source
     */
    public MetaDataSource createDefaultMetaDataSource() {
        return new DefaultMetaDataSource(this);
    }

    /**
     * Create new instance of customization meta data source.
     *
     * @return customization meta data source
     */
    public abstract CustomMetaDataSource createDefaultCustomMetaDataSource();

    /**
     * Execute an operation as logged-in client.
     *
     * @param op operation to be executed
     * @param <R> type of operation result
     * @return result of execution
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    protected <R> R executeUsingLogin(PortOperation<R, PortT> op) throws NetSuiteException {
        lock.lock();
        try {
            // Log in if required
            login(false);

            R result = null;
            for (int i = 0; i < getRetryCount(); i++) {
                try {
                    result = op.execute(port);
                    break;
                } catch (Exception e) {
                    if (errorCanBeWorkedAround(e)) {
                        logger.debug("Attempting workaround, retrying ({})", (i + 1));
                        waitForRetryInterval();
                        if (errorRequiresNewLogin(e) || i >= getRetriesBeforeLogin() - 1) {
                            logger.debug("Re-logging in ({})", (i + 1));
                            relogin();
                        }
                        continue;
                    } else {
                        throw new NetSuiteException(e.getMessage(), e);
                    }
                }
            }
            return result;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute an operation using request level credentials.
     *
     * @param op operation to be executed
     * @param <R> type of operation result
     * @return result of execution
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    private <R> R executeUsingRequestLevelCredentials(PortOperation<R, PortT> op) throws NetSuiteException {
        lock.lock();
        try {
            relogin();

            R result = null;
            for (int i = 0; i < getRetryCount(); i++) {
                try {
                    result = op.execute(port);
                    break;
                } catch (Exception e) {
                    if (errorCanBeWorkedAround(e)) {
                        logger.debug("Attempting workaround, retrying ({})", (i + 1));
                        waitForRetryInterval();
                        continue;
                    } else {
                        throw new NetSuiteException(e.getMessage(), e);
                    }
                }
            }
            return result;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Set a SOAP header to be sent to NetSuite in request
     *
     * @param port port
     * @param header header to be set
     */
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

    /**
     * Remove a SOAP header from header list to be sent to NetSuite
     *
     * @param name name identifying a header
     */
    protected void removeHeader(QName name) {
        removeHeader(port, name);
    }

    /**
     * Remove a SOAP header from header list to be sent to NetSuite
     *
     * @param port port
     * @param name name identifying a header
     */
    protected void removeHeader(PortT port, QName name) {
        BindingProvider provider = (BindingProvider) port;
        Map<String, Object> requestContext = provider.getRequestContext();
        List<Header> list = (List<Header>) requestContext.get(Header.HEADER_LIST);
        removeHeader(list, name);
    }

    /**
     * Remove a SOAP header from given header list.
     *
     * @param list header list
     * @param name name identifying a header
     */
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

    /**
     * Forcibly re-log in the client.
     *
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    private void relogin() throws NetSuiteException {
        login(true);
    }

    /**
     * Log in the client.
     *
     * @param relogin specifies whether the client should be forcibly re-logged in
     * @throws NetSuiteException if an error occurs during performing of operation
     */
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

    /**
     * Perform 'log out' operation.
     *
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    protected abstract void doLogout() throws NetSuiteException;

    /**
     * Perform 'log in' operation.
     *
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    protected abstract void doLogin() throws NetSuiteException;

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

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

    public int getRetriesBeforeLogin() {
        return retriesBeforeLogin;
    }

    public void setRetriesBeforeLogin(int retriesBeforeLogin) {
        this.retriesBeforeLogin = retriesBeforeLogin;
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

    /**
     * Check whether given error can be worked around by retrying.
     *
     * @param t error to be checked
     * @return {@code true} if the error can be worked around, {@code false} otherwise
     */
    protected abstract boolean errorCanBeWorkedAround(Throwable t);

    /**
     * Check whether given error can be requires new log-in.
     *
     * @param t error to be checked
     * @return {@code true} if the error requies new log-in, {@code false} otherwise
     */
    protected abstract boolean errorRequiresNewLogin(Throwable t);

    /**
     * Set preferences for given port.
     *
     * @param port port which to set preferences for
     * @param nsPreferences general preferences
     * @param nsSearchPreferences search preferences
     * @throws NetSuiteException if an error occurs during performing of operation
     */
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
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.INTERNAL_ERROR),
                    "XML binding error", e);
        }
    }

    /**
     * Set log-in specific SOAP headers for given port.
     *
     * @param port port
     * @throws NetSuiteException if an error occurs during performing of operation
     */
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
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.INTERNAL_ERROR),
                        "XML binding error", e);
            }
        }
    }

    /**
     * Remove log-in specific SOAP headers for given port.
     *
     * @param port port
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    protected void remoteLoginHeaders(PortT port) throws NetSuiteException {
        removeHeader(port, new QName(getPlatformMessageNamespaceUri(), "applicationInfo"));
    }

    /**
     * Set HTTP client policy for given port.
     *
     * @param port port
     */
    protected void setHttpClientPolicy(PortT port) {
        Client proxy = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(connectionTimeout);
        httpClientPolicy.setReceiveTimeout(receiveTimeout);
        conduit.setClient(httpClientPolicy);
    }

    /**
     * Get URI for 'platform message' namespace.
     *
     * @return namespace URI
     */
    protected abstract String getPlatformMessageNamespaceUri();

    /**
     * Create instance of NetSuite's {@code Preferences} native data object.
     *
     * @param nsPreferences source preferences data object
     * @param <T> type of native data object
     * @return {@code Preferences} data object
     */
    protected abstract <T> T createNativePreferences(NsPreferences nsPreferences);

    /**
     * Create instance of NetSuite's {@code SearchPreferences} native data object.
     *
     * @param nsSearchPreferences source search preferences data object
     * @param <T> type of native data object
     * @return {@code SearchPreferences} data object
     */
    protected abstract <T> T createNativeSearchPreferences(NsSearchPreferences nsSearchPreferences);

    /**
     * Create instance of NetSuite's {@code ApplicationInfo} native data object.
     *
     * @param nsCredentials credentials data object
     * @param <T> type of native data object
     * @return {@code ApplicationInfo} data object
     */
    protected abstract <T> T createNativeApplicationInfo(NetSuiteCredentials nsCredentials);

    /**
     * Create instance of NetSuite's {@code Passport} native data object.
     *
     * @param nsCredentials credentials data object
     * @param <T> type of native data object
     * @return {@code Passport} data object
     */
    protected abstract <T> T createNativePassport(NetSuiteCredentials nsCredentials);

    /**
     * Get instance of NetSuite web service port implementation.
     *
     * @param defaultEndpointUrl default URL of NetSuite endpoint
     * @param account  NetSuite account number
     * @return port
     * @throws NetSuiteException if an error occurs during performing of operation
     */
    protected abstract PortT getNetSuitePort(String defaultEndpointUrl, String account) throws NetSuiteException;

    /**
     * Check 'log-in' operation status and throw {@link NetSuiteException} if status indicates that
     * an error occurred or exception message is present.
     *
     * @param status status object to be checked, if present
     * @param exceptionMessage exception message, if present
     */
    protected void checkLoginError(NsStatus status, String exceptionMessage) {
        if (status == null || !status.isSuccess()) {
            StringBuilder sb = new StringBuilder();
            if (status != null && status.getDetails().size() > 0) {
                NsStatus.Detail detail = status.getDetails().get(0);
                sb.append(detail.getCode()).append(" ").append(detail.getMessage());
            } else if (exceptionMessage != null) {
                sb.append(exceptionMessage);
            }
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.failedToLogin", sb));
        }
    }

    /**
     * Operation that requires NetSuite port.
     *
     * @param <R> type of operation result
     * @param <PortT> type of NetSuite port implementation
     */
    public interface PortOperation<R, PortT> {
        R execute(PortT port) throws Exception;
    }

    /**
     * Check status of an operation and throw {@link NetSuiteException} if status indicates that
     * an error occurred.
     *
     * @param status status object to be checked
     * @throws NetSuiteException if status indicates an error
     */
    public static void checkError(NsStatus status) throws NetSuiteException {
        if (!status.getDetails().isEmpty()) {
            NsStatus.Detail detail = status.getDetails().get(0);
            if (detail.getType() == NsStatus.Type.ERROR) {
                throw new NetSuiteException(new NetSuiteErrorCode(detail.getCode()),
                        ExceptionContext.build().put(ExceptionContext.KEY_MESSAGE, detail.getMessage()));
            }
        }
    }

}
