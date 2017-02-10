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

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageDefinition;
import org.talend.components.azurestorage.table.runtime.AzureStorageTableSink;
import org.talend.components.azurestorage.table.runtime.AzureStorageTableSource;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 * Table:
 *
 * A table is a collection of entities. Tables don't enforce a schema on entities, which means a single table can
 * contain entities that have different sets of properties. The number of tables that a storage account can contain is
 * limited only by the storage account capacity limit.
 *
 * Entity:
 *
 * An entity is a set of properties, similar to a database row. An entity can be up to 1MB in size.
 *
 * Properties:
 *
 * A property is a name-value pair. Each entity can include up to 252 properties to store data. Each entity also has 3
 * system properties that specify a partition key, a row key, and a timestamp. Entities with the same partition key can
 * be queried more quickly, and inserted/updated in atomic operations. An entity's row key is its unique identifier
 * within a partition.
 *
 */
public abstract class AzureStorageTableDefinition extends AzureStorageDefinition {

    public AzureStorageTableDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Cloud/Azure Storage/Table" }; //$NON-NLS-1$
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        if (connectorTopology == ConnectorTopology.OUTGOING || connectorTopology == ConnectorTopology.NONE) {
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageTableSource.class);
        } else {
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageTableSink.class);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { AzureStorageTableProperties.class });
    }

}
