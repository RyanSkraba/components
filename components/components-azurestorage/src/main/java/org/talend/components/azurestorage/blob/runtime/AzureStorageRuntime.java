package org.talend.components.azurestorage.blob.runtime;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureConnection;
import org.talend.components.azurestorage.AzureConnectionWithKeyService;
import org.talend.components.azurestorage.AzureConnectionWithSasService;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.CloudStorageAccount;

public class AzureStorageRuntime implements RuntimableRuntime<ComponentProperties> {

    private static final long serialVersionUID = 8150539704549116311L;

    public static final String KEY_CONNECTION_PROPERTIES = "connection";

    public AzureStorageProvideConnectionProperties properties;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(AzureStorageRuntime.class);

    private static final String SAS_PATTERN = "(http.?)?://(.*)\\.(blob|file|queue|table)\\.(.*)/(.*)";

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        // init
        this.properties = (AzureStorageProvideConnectionProperties) properties;
        TAzureStorageConnectionProperties conn = getUsedConnection(runtimeContainer);

        if (runtimeContainer != null) {
            AzureStorageUtils.setApplicationVersion((String) runtimeContainer.getGlobalData(AzureStorageUtils.TALEND_PRODUCT_VERSION_GLOBAL_KEY));
            AzureStorageUtils.setComponentVersion((String) runtimeContainer.getGlobalData(AzureStorageUtils.TALEND_COMPONENT_VERSION_GLOBAL_KEY));
        }

        // Validate connection properties

        String errorMessage = "";
        if (conn == null) { // check connection failure

            errorMessage = messages.getMessage("error.VacantConnection"); //$NON-NLS-1$

        } else if (conn.useSharedAccessSignature.getValue()) { // checks
            if (StringUtils.isEmpty(conn.sharedAccessSignature.getStringValue())) {
                errorMessage = messages.getMessage("error.EmptySAS"); //$NON-NLS-1$
            } else {
                Matcher m = Pattern.compile(SAS_PATTERN).matcher(conn.sharedAccessSignature.getValue());
                if (!m.matches()) {
                    errorMessage = messages.getMessage("error.InvalidSAS");
                }
            }

        } else if (!conn.useSharedAccessSignature.getValue() && (StringUtils.isEmpty(conn.accountName.getStringValue())
                || StringUtils.isEmpty(conn.accountKey.getStringValue()))) { // checks connection's account and key

            errorMessage = messages.getMessage("error.EmptyKey"); //$NON-NLS-1$
        }

        // Return result
        if (errorMessage.isEmpty()) {
            return ValidationResult.OK;
        } else {
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
    }

    public TAzureStorageConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties();
    }

    public TAzureStorageConnectionProperties getUsedConnection(RuntimeContainer runtimeContainer) {
        TAzureStorageConnectionProperties connectionProperties = properties.getConnectionProperties();
        String refComponentId = connectionProperties.getReferencedComponentId();

        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (runtimeContainer != null) {
                TAzureStorageConnectionProperties sharedConn = (TAzureStorageConnectionProperties) runtimeContainer
                        .getComponentData(refComponentId, KEY_CONNECTION_PROPERTIES);
                if (sharedConn != null) {
                    return sharedConn;
                }
            }
            // Design time
            connectionProperties = connectionProperties.getReferencedConnectionProperties();
        }
        if (runtimeContainer != null) {
            runtimeContainer.setComponentData(runtimeContainer.getCurrentComponentId(), KEY_CONNECTION_PROPERTIES,
                    connectionProperties);
        }
        return connectionProperties;
    }

    public CloudStorageAccount getStorageAccount(RuntimeContainer runtimeContainer)
            throws URISyntaxException, InvalidKeyException {

        return getAzureConnection(runtimeContainer).getCloudStorageAccount();
    }

    public AzureConnection getAzureConnection(RuntimeContainer runtimeContainer) {

        TAzureStorageConnectionProperties conn = getUsedConnection(runtimeContainer);
        if (conn.useSharedAccessSignature.getValue()) {
            // extract account name and sas token from sas url
            Matcher m = Pattern.compile(SAS_PATTERN).matcher(conn.sharedAccessSignature.getValue());
            m.matches();

            return AzureConnectionWithSasService.builder()//
                    .accountName(m.group(2))//
                    .sasToken(m.group(5))//
                    .endpointSuffix(m.group(4))
                    .build();

        } else {

            return AzureConnectionWithKeyService.builder()//
                    .protocol(conn.protocol.getValue().toString().toLowerCase())//
                    .accountName(conn.accountName.getValue())//
                    .accountKey(conn.accountKey.getValue()).build();
        }

    }
}
