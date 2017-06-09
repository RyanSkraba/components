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
package org.talend.components.azurestorage.wizard;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerProperties;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.components.azurestorage.table.AzureStorageTableProperties;
import org.talend.components.azurestorage.table.runtime.AzureStorageTableSourceOrSink;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;

public class AzureStorageComponentListProperties extends ComponentPropertiesImpl
        implements AzureStorageProvideConnectionProperties {

    public static final String FORM_TABLE = "Table";

    public static final String FORM_QUEUE = "Queue";

    public static final String FORM_CONTAINER = "Container";

    private static final long serialVersionUID = 1962464678283327395L;

    private TAzureStorageConnectionProperties connection = new TAzureStorageConnectionProperties("connection");

    private String repositoryLocation;

    public Property<List<NamedThing>> selectedContainerNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedContainerNames"); //$NON-NLS-1$

    public Property<List<NamedThing>> selectedTableNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedTableNames"); //$NON-NLS-1$

    public Property<List<NamedThing>> selectedQueueNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedQueueNames"); //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageComponentListProperties.class);
    
    public AzureStorageComponentListProperties(String name) {
        super(name);
    }

    @Override
    public TAzureStorageConnectionProperties getConnectionProperties() {
        return connection;
    }

    public AzureStorageComponentListProperties setConnection(TAzureStorageConnectionProperties connection) {
        this.connection = connection;
        return this;
    }

    public AzureStorageComponentListProperties setRepositoryLocation(String repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form containerForm = Form.create(this, FORM_CONTAINER);
        containerForm.addRow(widget(selectedContainerNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(containerForm);

        Form queueForm = Form.create(this, FORM_QUEUE);
        queueForm.addRow(widget(selectedQueueNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(queueForm);

        Form tableForm = Form.create(this, FORM_TABLE);
        tableForm.addRow(widget(selectedTableNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(tableForm);
    }

    public void beforeFormPresentContainer(){
        selectedContainerNames.setPossibleValues(connection.BlobSchema);
        getForm(FORM_CONTAINER).setAllowBack(true);
        getForm(FORM_CONTAINER).setAllowForward(true);
        getForm(FORM_CONTAINER).setAllowFinish(true);
    }

    public void beforeFormPresentQueue(){
        selectedQueueNames.setPossibleValues(connection.QueueSchema);
        getForm(FORM_QUEUE).setAllowBack(true);
        getForm(FORM_QUEUE).setAllowForward(true);
        getForm(FORM_QUEUE).setAllowFinish(true);
    }

    public void beforeFormPresentTable(){
        selectedTableNames.setPossibleValues(connection.TableSchema);
        getForm(FORM_TABLE).setAllowBack(true);
        getForm(FORM_TABLE).setAllowFinish(true);
    }

    public ValidationResult afterFormFinishTable(Repository<Properties> repo) throws Exception {
        
        connection.BlobSchema = selectedContainerNames.getValue();
        connection.QueueSchema = selectedQueueNames.getValue();
        connection.TableSchema = selectedTableNames.getValue();
        
        String repoLoc = repo.storeProperties(connection, connection.name.getValue(), repositoryLocation, null);

        String storeId;
        if (selectedContainerNames.getValue() != null) {
            for (NamedThing nl : selectedContainerNames.getValue()) {
                String containerId = nl.getName();
                AzureStorageContainerProperties containerProps = new AzureStorageContainerProperties(containerId);
                containerProps.init();
                containerProps.connection = connection;
                containerProps.container.setValue(containerId);
                containerProps.schema.schema.setValue(getContainerSchema());
                repo.storeProperties(containerProps, formatSchemaName(containerId), repoLoc, "schema.schema");
            }
        }
        if (selectedQueueNames.getValue() != null) {
            for (NamedThing nl : selectedQueueNames.getValue()) {
                String queueId = nl.getName();
                AzureStorageQueueProperties queueProps = new AzureStorageQueueProperties(queueId);
                queueProps.init();
                queueProps.connection = connection;
                queueProps.queueName.setValue(queueId);
                queueProps.schema.schema.setValue(getQueueSchema());
                repo.storeProperties(queueProps, formatSchemaName(queueId), repoLoc, "schema.schema");
            }
        }
        if (selectedTableNames.getValue() != null) {
            for (NamedThing nl : selectedTableNames.getValue()) {
                String tableId = nl.getName();
                AzureStorageTableProperties tableProps = new AzureStorageTableProperties(tableId);
                tableProps.init();
                tableProps.connection = connection;
                tableProps.tableName.setValue(tableId);
                try {
                    Schema schema = AzureStorageTableSourceOrSink.getSchema(null, connection, tableId);
                    tableProps.schema.schema.setValue(schema);
                    repo.storeProperties(tableProps, formatSchemaName(tableId), repoLoc, "schema.schema");
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }
        return ValidationResult.OK;
    }
    
    private String formatSchemaName(String name){
        String storeId = name.replaceAll("-", "_").replaceAll(" ", "_");
        if(Character.isDigit(storeId.charAt(0))){
            storeId = "_"+storeId;
        }
        return storeId;
    }

    public Schema getContainerSchema() {
        return SchemaBuilder.builder().record("Main").fields()//
                .name("containerName").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "100").type(AvroUtils._string()).noDefault()//
                .endRecord();
    }

    public Schema getQueueSchema() {
        return SchemaBuilder.builder().record("Main").fields()//
                .name(AzureStorageQueueProperties.FIELD_MESSAGE_ID).prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "100").type(AvroUtils._string()).noDefault()//
                .name(AzureStorageQueueProperties.FIELD_MESSAGE_CONTENT).type(AvroUtils._string()).noDefault() //
                .name(AzureStorageQueueProperties.FIELD_INSERTION_TIME)
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss").type(AvroUtils._date()).noDefault() //
                .name(AzureStorageQueueProperties.FIELD_EXPIRATION_TIME)
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss").type(AvroUtils._date()).noDefault() //
                .name(AzureStorageQueueProperties.FIELD_NEXT_VISIBLE_TIME)
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss").type(AvroUtils._date()).noDefault() //
                .name(AzureStorageQueueProperties.FIELD_DEQUEUE_COUNT).type(AvroUtils._int()).noDefault() //
                .name(AzureStorageQueueProperties.FIELD_POP_RECEIPT).type(AvroUtils._string()).noDefault() //
                .endRecord();
    }

}
