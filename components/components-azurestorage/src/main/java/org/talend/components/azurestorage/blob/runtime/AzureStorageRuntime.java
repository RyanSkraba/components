package org.talend.components.azurestorage.blob.runtime;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.utils.SharedAccessSignatureUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageRuntime implements RuntimableRuntime<ComponentProperties> {

    private static final long serialVersionUID = 8150539704549116311L;

    public static final String KEY_CONNECTION_PROPERTIES = "connection";

    public AzureStorageProvideConnectionProperties properties;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(AzureStorageRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        // init
        this.properties = (AzureStorageProvideConnectionProperties) properties;
        TAzureStorageConnectionProperties conn = getUsedConnection(runtimeContainer);

        // Validate connection properties

        String errorMessage = "";
        if (conn == null) { // check connection failure

            errorMessage = messages.getMessage("error.VacantConnection"); //$NON-NLS-1$

        } else if (conn.useSharedAccessSignature.getValue() && StringUtils.isEmpty(conn.sharedAccessSignature.getStringValue())) { // checks
                                                                                                                                   // SAS
            errorMessage = messages.getMessage("error.EmptySAS"); //$NON-NLS-1$
        } else if (!conn.useSharedAccessSignature.getValue() && (StringUtils.isEmpty(conn.accountName.getStringValue())
                || StringUtils.isEmpty(conn.accountKey.getStringValue()))) { // checks connection's account and key

            errorMessage = messages.getMessage("error.EmptyKey"); //$NON-NLS-1$
        }

        // Return result
        if (errorMessage.isEmpty()) {
            return ValidationResult.OK;
        } else {
            return createValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
    }



    public TAzureStorageConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties();
    }

    public TAzureStorageConnectionProperties getUsedConnection(RuntimeContainer runtimeContainer) {
        TAzureStorageConnectionProperties connectionProperties = ((AzureStorageProvideConnectionProperties) properties)
                .getConnectionProperties();
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

        CloudStorageAccount account;
        TAzureStorageConnectionProperties conn = getUsedConnection(runtimeContainer);
        if (conn.useSharedAccessSignature.getValue()) {
            SharedAccessSignatureUtils sas = SharedAccessSignatureUtils
                    .getSharedAccessSignatureUtils(conn.sharedAccessSignature.getValue());
            StorageCredentials credentials = new StorageCredentialsSharedAccessSignature(sas.getSharedAccessSignature());
            account = new CloudStorageAccount(credentials, true, null, sas.getAccount());

        } else {
            StringBuilder connectionString = new StringBuilder();
            connectionString.append("DefaultEndpointsProtocol=")
                    .append(conn.protocol.getValue().toString().toLowerCase())
                    //
                    .append(";AccountName=").append(conn.accountName.getValue())
                    //
                    .append(";AccountKey=").append(conn.accountKey.getValue());
            account = CloudStorageAccount.parse(connectionString.toString());
        }

        return account;
    }

    /**
     * getServiceClient.
     *
     * @param runtimeContainer {@link RuntimeContainer} container
     * @return {@link CloudBlobClient} cloud blob client
     */
    public CloudBlobClient getServiceClient(RuntimeContainer runtimeContainer) throws InvalidKeyException, URISyntaxException {
        return getStorageAccount(runtimeContainer).createCloudBlobClient();
    }

    /**
     * getStorageContainerReference.
     *
     * @param runtimeContainer {@link RuntimeContainer} container
     * @param containerName {@link String} storage container
     * @return {@link CloudBlobContainer} cloud blob container
     * @throws StorageException
     * @throws URISyntaxException
     * @throws InvalidKeyException
     */
    public CloudBlobContainer getAzureStorageBlobContainerReference(RuntimeContainer runtimeContainer, String containerName)
            throws InvalidKeyException, URISyntaxException, StorageException {

        return getServiceClient(runtimeContainer).getContainerReference(containerName);
    }

    public ValidationResult createValidationResult(ValidationResult.Result resultCode, String resultMessage) {
        ValidationResult vr = new ValidationResult();
        vr.setMessage(resultMessage); // $NON-NLS-1$
        vr.setStatus(resultCode);
        return vr;
    }

}
