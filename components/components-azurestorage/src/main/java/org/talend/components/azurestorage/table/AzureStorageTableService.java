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
package org.talend.components.azurestorage.table;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.azurestorage.AzureConnection;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;

import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableResult;
import com.microsoft.azure.storage.table.TableServiceException;

public class AzureStorageTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageTableService.class);

    private AzureConnection connection;

    public AzureStorageTableService(final AzureConnection connection) {
        super();
        this.connection = connection;
    }

    public Iterable<String> listTables() throws InvalidKeyException, URISyntaxException {
        CloudTableClient cloudTableClient = connection.getCloudStorageAccount().createCloudTableClient();
        return cloudTableClient.listTables();
    }

    public Iterable<DynamicTableEntity> executeQuery(String tableName, TableQuery<DynamicTableEntity> partitionQuery)
            throws InvalidKeyException, URISyntaxException, StorageException {

        CloudTable cloudTable = connection.getCloudStorageAccount().createCloudTableClient().getTableReference(tableName);
        return cloudTable.execute(partitionQuery);
    }

    public void handleActionOnTable(String tableName, ActionOnTable actionTable)
            throws IOException, StorageException, InvalidKeyException, URISyntaxException {

        // FIXME How does this will behave in a distributed runtime ? See where to place correctly this
        // instruction...
        CloudTable cloudTable = connection.getCloudStorageAccount().createCloudTableClient().getTableReference(tableName);
        switch (actionTable) {
        case Create_table:
            cloudTable.create();
            break;
        case Create_table_if_does_not_exist:
            cloudTable.createIfNotExists();
            break;
        case Drop_and_create_table:
            cloudTable.delete();
            createTableAfterDeletion(cloudTable);
            break;
        case Drop_table_if_exist_and_create:
            cloudTable.deleteIfExists();
            createTableAfterDeletion(cloudTable);
            break;
        case Default:
        default:
            return;
        }

    }

    public TableResult executeOperation(String tableName, TableOperation ope)
            throws InvalidKeyException, URISyntaxException, StorageException {

        CloudTable cloudTable = connection.getCloudStorageAccount().createCloudTableClient().getTableReference(tableName);
        return cloudTable.execute(ope);
    }

    public ArrayList<TableResult> executeOperation(String tableName, TableBatchOperation batchOpe)
            throws InvalidKeyException, URISyntaxException, StorageException {

        CloudTable cloudTable = connection.getCloudStorageAccount().createCloudTableClient().getTableReference(tableName);
        return cloudTable.execute(batchOpe);
    }

    /**
     * This method create a table after it's deletion.<br/>
     * the table deletion take about 40 seconds to be effective on azure CF.
     * https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/Delete-Table#Remarks <br/>
     * So we try to wait 50 seconds if the first table creation return an
     * {@link StorageErrorCodeStrings.TABLE_BEING_DELETED } exception code
     * 
     * @param cloudTable
     * 
     * @throws StorageException
     * @throws IOException
     * 
     */
    private void createTableAfterDeletion(CloudTable cloudTable) throws StorageException, IOException {
        try {
            cloudTable.create();
        } catch (TableServiceException e) {
            if (!e.getErrorCode().equals(StorageErrorCodeStrings.TABLE_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn("Table '{}' is currently being deleted. We'll retry in a few moments...", cloudTable.getName());
            // wait 50 seconds (min is 40s) before retrying.
            // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/Delete-Table#Remarks
            try {
                Thread.sleep(50000);
            } catch (InterruptedException eint) {
                throw new IOException("Wait process for recreating table interrupted.");
            }
            cloudTable.create();
            LOGGER.debug("Table {} created.", cloudTable.getName());
        }
    }

}
