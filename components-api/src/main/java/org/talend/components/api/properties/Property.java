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

import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.SimpleNamedThing;
import org.talend.components.api.properties.internal.ComponentPropertiesInternal;
import org.talend.components.api.schema.AbstractSchemaElement;

/**
 * A property that is part of a {@link ComponentProperties}.
 */
public class Property extends AbstractSchemaElement {

    private static final String I18N_PROPERTY_PREFIX = "property."; //$NON-NLS-1$

    protected ComponentPropertiesInternal componentProperties;

    protected EnumSet<Flags> flags;

    private Map<String, Object> taggedValues = new HashMap<String, Object>();

    public enum Flags {
                       /**
                        * Encrypt this when storing the {@link ComponentProperties} into a serializable form.
                        */
        ENCRYPT,
                       /**
                        * Show this in the UI as if it were a password.
                        */
        UI_PASSWORD,
                       /**
                        * Don't log this value in any logs.
                        */
        SUPPRESS_LOGGING;
    };

    public Property(String name) {
        this(name, null);
    }

    public Property(String name, String title) {
        this(null, name, title);
    }

    public Property(Type type, String name, String title) {
        setName(name);
        setType(type == null ? Type.STRING : type);
        setTitle(title);
        setSize(-1);
    }

    public Property(Type type, String name) {
        this(type, name, null);
    }

    public EnumSet<Flags> getFlags() {
        return flags;
    }

    public Property setFlags(EnumSet<Flags> flags) {
        this.flags = flags;
        return this;
    }

    public boolean isFlag(Flags flag) {
        if (flags == null) {
            return false;
        }
        return flags.contains(flag);
    }

    public void setValue(Object value) {
        componentProperties.setValue(this, value);
    }

    public Object getValue() {
        return componentProperties.getValue(this);
    }

    public boolean getBooleanValue() {
        return componentProperties.getBooleanValue(this);
    }

    public String getStringValue() {
        return componentProperties.getStringValue(this);
    }

    public int getIntValue() {
        return componentProperties.getIntValue(this);
    }

    public Calendar getCalendarValue() {
        return componentProperties.getCalendarValue(this);
    }

    @Override
    public String toString() {
        return "Property: " + getName();
    }

    // Not API
    void setValueHolder(ComponentPropertiesInternal props) {
        componentProperties = props;
    }

    /**
     * If no displayName was specified then the i18n key : {@value Property#I18N_PROPERTY_PREFIX}.name_of_this_property.
     * {@value SimpleNamedThing#I18N_DISPLAY_NAME_SUFFIX} to find the value from the i18n.
     */
    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getI18nMessage(I18N_PROPERTY_PREFIX + name + I18N_DISPLAY_NAME_SUFFIX);
    }

    /**
     * This store a value with the given key in a map this will be serialized with the component. This may be used to
     * identify the context of the value, whether is may be some java string or some context value or system properties.
     * Use this tag a will as long as the value is serializable.
     * 
     * @param key, key to store the object with
     * @param value, any serializable object.
     */
    public void setTaggedValue(String key, Object value) {
        taggedValues.put(key, value);
    }

    /**
     * return the previously stored value using {@link Property#setTaggedValue(String, Object)} and the given key.
     * 
     * @param key, identify the value to be fetched
     * @return the object stored along with the key or null if none found.
     */
    public Object getTaggedValue(String key) {
        return taggedValues.get(key);
    }

}
