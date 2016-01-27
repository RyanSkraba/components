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
import org.talend.components.api.schema.AbstractSchemaElement;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaFactory;

/**
 * A property that is part of a {@link ComponentProperties}.
 */
public class Property extends AbstractSchemaElement {

    private static final String I18N_PROPERTY_PREFIX = "property."; //$NON-NLS-1$

    protected EnumSet<Flags> flags;

    private Map<String, Object> taggedValues = new HashMap<>();

    private Object storedValue;

    transient private PropertyValueEvaluator propertyValueEvaluator;

    public enum Flags {
                       /**
                        * Encrypt this when storing the {@link ComponentProperties} into a serializable form.
                        */
        ENCRYPT,
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
        Object valueToSet = value;
        if (getType() == Type.SCHEMA && value instanceof String) {
            valueToSet = SchemaFactory.fromSerialized((String) value);
        }
        storedValue = valueToSet;
    }

    public Object getStoredValue() {
        return storedValue;
    }

    /**
     * @return the value of the property. This value may not be the one Stored with setValue(), it may be evaluated with
     * {@link PropertyValueEvaluator}.
     * 
     * 
     */
    public Object getValue() {
        if (propertyValueEvaluator != null) {
            return propertyValueEvaluator.evaluate(this, storedValue);
        } // else not evaluator so return the storedValue
        return storedValue;
    }

    /**
     * @return cast the getValue() into a boolean or return false if null.
     */
    public boolean getBooleanValue() {
        return Boolean.valueOf(String.valueOf(getValue()));
    }

    /**
     * @return cast the getValue() into a String.
     */
    public String getStringValue() {
        Object value = getValue();
        if (value != null) {
            if (value instanceof Schema) {
                return ((Schema) value).toSerialized();
            }
            return String.valueOf(value);
        }
        return null;
    }

    /**
     * @return convert the storedValue to an int, return 0 if values is null.
     */
    public int getIntValue() {
        Object value = getValue();
        if (value == null) {
            return 0;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    /**
     * @return Cast the stored value to a Calendar.
     */
    public Calendar getCalendarValue() {
        return (Calendar) getValue();
    }

    @Override
    public String toString() {
        return "Property: " + getName();
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

    /**
     * DOC sgandon Comment method "setValueEvaluator".
     * 
     * @param ve
     */
    public void setValueEvaluator(PropertyValueEvaluator ve) {
        this.propertyValueEvaluator = ve;
    }

}
