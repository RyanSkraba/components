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
     * A special property for the values that a component returns. If this is used, this will be a {@link Property}
     * that contains each of the values the component returns.
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

}
