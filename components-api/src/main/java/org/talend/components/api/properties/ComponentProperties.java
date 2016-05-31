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
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesVisitor;
import org.talend.daikon.properties.property.Property;

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
    public Property<String> returns;

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
     * @param isOutgoingConnection wheter the connection is an outgoint connection or an incoming one.
     * @return the schema related to the connection or null if none.
     */
    public Schema getSchema(Connector connector, boolean isOutgoingConnection) {
        return null;
    }

    /**
     * return the set of all possible connectors for this component properties.
     * This is mainly ther for the Studio and shall not be an service API for that reason.
     * The list of connector returns by this should always be the same whatever the state of the component.
     * 
     * @param isOutgoingConnection true for getting all outgoing connectors for the Properties.
     * @return set of connection eventually proposed by this Properties.
     */
    public Set<? extends Connector> getPossibleConnectors(boolean isOutgoingConnection) {
        return Collections.EMPTY_SET;
    }

    /**
     * return the set of available connector that may be setup with a schema according to the current state of this instance.
     * The list of connector may differ according to the internal state of the component. This is up to the component developer
     * to decide which connector is avaialble and when.
     * 
     * @param existingConnectors list of connections already connected that may be of use to compute what remains to be
     *            connected.
     * @param isOutgoingConnection wether we query the possible output or input connections.
     * @return set of connection left to be connected, never null.
     */
    public Set<? extends Connector> getAvailableConnectors(Set<? extends Connector> existingConnectors,
            boolean isOutgoingConnection) {
        return Collections.EMPTY_SET;
    }

    /**
     * set the schema related to the connector with the schema at the other end of the connection.
     * 
     * @param connector used to identify which schema to set.
     * @param schema schema to set.
     * @param isOutgoingConnection true if connector is an outgoing connectors.
     * 
     */
    public void setConnectedSchema(Connector connector, Schema schema, boolean isOutgoingConnection) {
        // do nothing by default.

    }

    /**
     * this will look for all authorized nested properties that shall be compatible with the nestedValues and copy all
     * nestedValues properties into this.<br>
     * This default implementation will look for all nested properties that matches the type of nestedValues and copy
     * all nestedValues properties into it. This way of course be overriden if some component want to prevent the copy
     * of a given type into it.
     * 
     * @param nestedValues values to be used update this current ComponentProperties nested Properties.
     * @return true if the copy was done and false if the targetProperties does not accept the nestedValues type.
     */
    public boolean updateNestedProperties(final ComponentProperties nestedValues) {
        if (nestedValues == null) {
            return false;
        } // else not null so perfect
        final AtomicBoolean isCopied = new AtomicBoolean(false);
        accept(new PropertiesVisitor() {

            @Override
            public void visit(Properties properties, Properties parent) {
                if (properties.getClass().isAssignableFrom(nestedValues.getClass())) {
                    properties.copyValuesFrom(nestedValues);
                    isCopied.set(true);
                } // else not a compatible nestedValues so keep going
            }

        }, this);
        return isCopied.get();
    }
}
