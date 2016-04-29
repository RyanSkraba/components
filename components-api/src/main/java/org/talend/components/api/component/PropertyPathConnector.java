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
package org.talend.components.api.component;

import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.Property;

/**
 * class used to identify connections with a given {@link Property} or {@link Properties} by using their path relative
 * to a ComponentProperties.
 */
public class PropertyPathConnector extends SimpleNamedThing implements Connector {

    private String propertyPath;

    /**
     * 
     * @param type type of the connector, there may be multiple connector with the same type
     * @param propertyPath, path see {@link Properties#getProperty(String)} relative a ComponentProperties that will
     * provide this connector.
     */
    public PropertyPathConnector(String name, String propertyPath) {
        super(name);
        this.propertyPath = propertyPath;
    }

    @Override
    public String getDisplayName() {
        return getI18nMessage(Connector.I18N_PREFIX + getName() + I18N_DISPLAY_NAME_SUFFIX);
    }

    /**
     * @return the path see {@link Properties#getProperty(String)} relative a ComponentProperties that will provide this
     * connector.
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertyPathConnector other = (PropertyPathConnector) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
