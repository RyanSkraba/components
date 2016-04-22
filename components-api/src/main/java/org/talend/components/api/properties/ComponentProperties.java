// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.properties;

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.Property;

/**
 * for all details see {@link Properties}. This class adds a specific {@link ComponentProperties#returns} property which
 * is a schema element defining what is the type of data the component ouputs.
 */

public abstract class ComponentProperties extends Properties {

    /**
     * Name of the special Returns property.
     */
    public static final String RETURNS = "returns";

    /**
     * A special property for the values that a component returns. If this is used, this will be a {@link Property} that
     * contains each of the values the component returns.
     */
    public Property returns;

    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     * 
     * @param name, uniquely identify the property among other properties when used as nested properties.
     */
    public ComponentProperties(String name) {
        super(name);
    }

    @Override
    protected boolean acceptUninitializedField(Field f) {
        // we accept that return field is not intialized after setupProperties.
        return RETURNS.equals(f.getName());
    }

    /**
     * return the schema associated with the connection name on input or output if any
     * 
     * @param connector token to get the associated schema
     * @param isOutputConnection wheter the connection is an output connection in case output and inputs have same names
     * @return the schema related to the connection or null if none.
     */
    public Schema getSchema(Connector connector, boolean isOutputConnection) {
        return null;
    }

    /**
     * return the set of possible connections name that may be setup with a schema.
     * 
     * @param existingConnectors list of connections already connected that may be of use to compute what remains to be
     * connected.
     * @param isOutputConnection wether we query the possible output or input connections.
     * @return set of connection left to be connected.
     */
    public Set<? extends Connector> getAvailableConnectors(Set<? extends Connector> existingConnectors,
            boolean isOutputConnection) {
        return null;
    }

    /**
     * set the schema related to the connector with the schema at the other end of the connection.
     * 
     * @param connector used to identify which schema to set.
     * @param schema schema to set.
     * @param isOutputConnection wether the connector is an output connector.
     * 
     */
    public void setConnectedSchema(Connector connector, Schema schema, boolean isOutputConnection) {
        // do nothing by default.

    }
}
