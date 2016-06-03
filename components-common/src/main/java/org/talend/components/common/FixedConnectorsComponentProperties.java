// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common;

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * this class provide a simple mechanism for handle Properties component with a fixed set of connection (hence a fixed
 * set of schemas )in {@link Property}. This is supposed to be used a direct ComponentProperties and not nested ones.
 */
public abstract class FixedConnectorsComponentProperties extends ComponentPropertiesImpl {

    /**
     * FixedSchemaComponentProperties constructor comment.
     * 
     * @param name
     */
    public FixedConnectorsComponentProperties(String name) {
        super(name);
    }

    /**
     * This default implementation uses {@link PropertyPathConnector} to find the SchemaProperties or Property of type
     * Schema instances avaialble in this Object. It return null if none found
     */
    @SuppressWarnings("unchecked")
    @Override
    public Schema getSchema(Connector connector, boolean isOutputConnection) {
        if (connector instanceof PropertyPathConnector) {
            NamedThing property = getProperty(((PropertyPathConnector) connector).getPropertyPath());
            if (property != null) {
                Property<Schema> schemaProp = null;
                if (property instanceof SchemaProperties) {
                    SchemaProperties schemaProperties = (SchemaProperties) property;
                    schemaProp = schemaProperties.schema;
                } else if (property instanceof Property) {
                    schemaProp = (Property<Schema>) property;
                }
                return schemaProp != null ? schemaProp.getValue() : null;
            } else {// else path not found so throw exception
                throw new ComponentException(ComponentsErrorCode.WRONG_CONNECTOR,
                        ExceptionContext.build().put("properties", this.getClass().getCanonicalName()));
            }
        } // not a connector handled by this class
        return null;
    }

    /**
     * provide the list of all {@link PropertyPathConnector} related to the supported schemas properties used for input
     * (isOutputConnection=false) or output (isOutputConnection=true). The paths may refer to a Property of type Schema
     * (see {@link Property.Type#SCHEMA} and see {@link PropertyFactory#newSchema(String)}) or a SchemaProperties. The
     * path may be used by {@link #getProperty(String)}.
     */
    abstract protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection);

    @Override
    public Set<? extends Connector> getPossibleConnectors(boolean isOutputConnection) {
        return getAllSchemaPropertiesConnectors(isOutputConnection);
    }

    /**
     * this implmentation simply compute the diff between all connection names returned by
     * {@link #getAllSchemaPropertiesConnectors(boolean)} and the existingConnectors.
     * 
     * @param existingConnectors list of connectors already connected that may be of use to compute what remains to be
     *            connected.
     * @param isOutputConnection wether we query the possible output or input connections.
     * @return set of connector left to be connected.
     */
    @Override
    public Set<Connector> getAvailableConnectors(Set<? extends Connector> existingConnectors, boolean isOutputConnection) {
        return computeDiff(getAllSchemaPropertiesConnectors(isOutputConnection), existingConnectors);
    }

    private Set<Connector> computeDiff(Set<? extends Connector> allConnectors, Set<? extends Connector> existingConnections) {
        Set<Connector> diff = new HashSet<>(allConnectors);
        if (existingConnections != null) {
            diff.removeAll(existingConnections);
        } // else null so nothing to remove
        return diff;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setConnectedSchema(Connector connector, Schema schema, boolean isOutputConnection) {
        if (connector instanceof PropertyPathConnector) {
            NamedThing property = getProperty(((PropertyPathConnector) connector).getPropertyPath());
            if (property != null) {
                if (property instanceof SchemaProperties) {
                    SchemaProperties schemaProperties = (SchemaProperties) property;
                    schemaProperties.schema.setValue(schema);
                } else if (property instanceof Property) {
                    ((Property<Schema>) property).setValue(schema);
                }
            } else {// else path not found so throw exception
                throw new ComponentException(ComponentsErrorCode.WRONG_CONNECTOR,
                        ExceptionContext.build().put("properties", this.getClass().getCanonicalName()));
            }
        } // not a connector handled by this class
    }
}
