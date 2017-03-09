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
package org.talend.components.azurestorage.blob.runtime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.utils.SharedAccessSignatureUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageSourceOrSink implements SourceOrSink {

    public static final String KEY_CONNECTION_PROPERTIES = "connection";

    private static final long serialVersionUID = 1589394346101991075L;

    public AzureStorageProvideConnectionProperties properties;

    protected transient Schema schema;
    
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageSourceOrSink.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (AzureStorageProvideConnectionProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        TAzureStorageConnectionProperties conn = validateConnection(container);
        if (conn.useSharedAccessSignature.getValue()) {
            // checks SAS
            if (StringUtils.isEmpty(conn.sharedAccessSignature.getStringValue())) {
                ValidationResult vr = new ValidationResult();
                vr.setMessage(messages.getMessage("error.EmptySAS")); //$NON-NLS-1$
                vr.setStatus(ValidationResult.Result.ERROR);
                return vr;
            }
        } else {
            // checks connection's account and key
            if (StringUtils.isEmpty(conn.accountName.getStringValue()) || StringUtils.isEmpty(conn.accountKey.getStringValue())) {
                ValidationResult vr = new ValidationResult();
                vr.setMessage(messages.getMessage("error.EmptyKey")); //$NON-NLS-1$
                vr.setStatus(ValidationResult.Result.ERROR);
                return vr;
            }
        }
        return ValidationResult.OK;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    public static ValidationResult validateConnection(AzureStorageProvideConnectionProperties properties) {
        ValidationResult vr = new ValidationResult().setStatus(Result.OK);
        try {
            AzureStorageSourceOrSink sos = new AzureStorageSourceOrSink();
            sos.initialize(null, (ComponentProperties) properties);
            sos.getStorageAccount(null);
        } catch (InvalidKeyException | URISyntaxException e) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(e.getLocalizedMessage());
        }
        return vr;
    }

    public TAzureStorageConnectionProperties validateConnection(RuntimeContainer container) {
        TAzureStorageConnectionProperties connProps = getConnectionProperties();
        String refComponentId = connProps.getReferencedComponentId();
        TAzureStorageConnectionProperties sharedConn;
        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (container != null) {
                sharedConn = (TAzureStorageConnectionProperties) container.getComponentData(refComponentId,
                        KEY_CONNECTION_PROPERTIES);
                if (sharedConn != null) {
                    return sharedConn;
                }
            }
            // Design time
            connProps = connProps.getReferencedConnectionProperties();
        }
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), KEY_CONNECTION_PROPERTIES, connProps);
        }
        return connProps;
    }

    public TAzureStorageConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties();
    }

    public CloudStorageAccount getStorageAccount(RuntimeContainer container) throws URISyntaxException, InvalidKeyException {
        TAzureStorageConnectionProperties conn = validateConnection(container);
        CloudStorageAccount account;
        if (conn.useSharedAccessSignature.getValue()) {
            SharedAccessSignatureUtils sas = SharedAccessSignatureUtils
                    .getSharedAccessSignatureUtils(conn.sharedAccessSignature.getValue());
            StorageCredentials credentials = new StorageCredentialsSharedAccessSignature(sas.getSharedAccessSignature());
            account = new CloudStorageAccount(credentials, true, null, sas.getAccount());

        } else {
            String acct = conn.accountName.getValue();
            String key = conn.accountKey.getValue();
            String protocol = conn.protocol.getValue().toString().toLowerCase();
            String storageConnectionString = "DefaultEndpointsProtocol=" + protocol + ";" + "AccountName=" + acct + ";"
                    + "AccountKey=" + key;
            account = CloudStorageAccount.parse(storageConnectionString);
        }

        return account;
    }

    /**
     * getServiceClient.
     *
     * @param container {@link RuntimeContainer} container
     * @return {@link CloudBlobClient} cloud blob client
     */
    public CloudBlobClient getServiceClient(RuntimeContainer container) throws InvalidKeyException, URISyntaxException {
        return getStorageAccount(container).createCloudBlobClient();
    }

    /**
     * getStorageContainerReference.
     *
     * @param container {@link RuntimeContainer} container
     * @param storageContainer {@link String} storage container
     * @return {@link CloudBlobContainer} cloud blob container
     * @throws StorageException
     * @throws URISyntaxException
     * @throws InvalidKeyException
     */
    public CloudBlobContainer getStorageContainerReference(RuntimeContainer container, String storageContainer)
            throws InvalidKeyException, URISyntaxException, StorageException {
        return getServiceClient(container).getContainerReference(storageContainer);
    }

    public static List<NamedThing> getSchemaNames(RuntimeContainer container, TAzureStorageConnectionProperties properties)
            throws IOException {
        AzureStorageSourceOrSink sos = new AzureStorageSourceOrSink();
        sos.initialize(container, properties);
        return sos.getSchemaNames(container);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> result = new ArrayList<>();
        try {
            CloudBlobClient client = getServiceClient(container);
            for (CloudBlobContainer c : client.listContainers()) {
                result.add(new SimpleNamedThing(c.getName(), c.getName()));
            }
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new ComponentException(e);
        }
        return result;
    }

}
