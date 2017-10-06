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

package org.talend.components.bigquery.output;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.bigquery.BigQueryDatasetDefinition;
import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BigQueryOutputProperties extends FixedConnectorsComponentProperties implements IOProperties {

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public ReferenceProperties<BigQueryDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            BigQueryDatasetDefinition.NAME);

    public Property<TableOperation> tableOperation = PropertyFactory.newEnum("tableOperation", TableOperation.class);

    public Property<WriteOperation> writeOperation = PropertyFactory.newEnum("writeOperation", WriteOperation.class);

    public BigQueryOutputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            return Collections.EMPTY_SET;
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        tableOperation.setValue(TableOperation.NONE);
        writeOperation.setValue(WriteOperation.APPEND);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(tableOperation);
        mainForm.addRow(writeOperation);
    }

    public void afterTableOperation() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        form.getWidget(writeOperation).setVisible(tableOperation.getValue() == TableOperation.NONE
                || tableOperation.getValue() == TableOperation.CREATE_IF_NOT_EXISTS);
    }

    @Override
    public BigQueryDatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }

    public enum TableOperation {
        /**
         * Specifics that tables should not be created.
         *
         * <p>
         * If the output table does not exist, the write fails.
         */
        NONE,
        /**
         * Specifies that tables should be created if needed. This is the default behavior.
         *
         * <p>
         * Requires that a table schema is provided via {@link BigQueryDatasetProperties#main}. This precondition is
         * checked before starting a job. The schema is not required to match an existing table's schema.
         *
         * <p>
         * When this transformation is executed, if the output table does not exist, the table is created from the
         * provided schema.
         */
        CREATE_IF_NOT_EXISTS,
        /**
         * Specifies that tables should be droped if exists, and create by the provided schema, which actually the
         * combine with TRUNCATE and CREATE_IF_NOT_EXISTS
         */
        DROP_IF_EXISTS_AND_CREATE,
        /**
         * Specifies that write should replace a table.
         *
         * <p>
         * The replacement may occur in multiple steps - for instance by first removing the existing table, then
         * creating a replacement, then filling it in. This is not an atomic operation, and external programs may see
         * the table in any of these intermediate steps.
         */
        TRUNCATE,
    }

    public enum WriteOperation {
        /**
         * Specifies that rows may be appended to an existing table.
         */
        APPEND,
        /**
         * Specifies that the output table must be empty. This is the default behavior.
         *
         * <p>
         * If the output table is not empty, the write fails at runtime.
         *
         * <p>
         * This check may occur long before data is written, and does not guarantee exclusive access to the table. If
         * two programs are run concurrently, each specifying the same output table and a {@link WriteOperation} of
         * {@link WriteOperation#WRITE_TO_EMPTY}, it is possible for both to succeed.
         */
        WRITE_TO_EMPTY
    }
}
