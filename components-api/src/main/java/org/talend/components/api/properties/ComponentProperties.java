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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.validation.constraints.NotNull;

import org.talend.components.api.ComponentDesigner;
import org.talend.components.api.ToStringIndent;
import org.talend.components.api.ToStringIndentUtil;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.i18n.TranslatableImpl;
import org.talend.components.api.properties.internal.ComponentPropertiesInternal;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.SchemaFactory;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.security.CryptoHelper;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

/**
 * The {@code ComponentProperties} class contains the definitions of the properties associated with a component. These
 * definitions contain enough information to automatically construct a nice looking user interface (UI) to populate and
 * validate the properties. The objective is that no actual (graphical) UI code is included in the component's
 * definition and as well no custom graphical UI is required for most components. The types of UIs that can be defined
 * include those for desktop (Eclipse), web, and scripting. All of these will use the code defined here for their
 * construction and validation.
 * <p/>
 * All aspects of the properties are defined in a subclass of this class using the {@link Property},
 * {@Link PresentationItem}, {@link Widget}, and {@link Form} classes. In addition in cases where user interface
 * decisions are made in code, methods can be added to the subclass to influence the flow of the user interface and help
 * with validation.
 * <p/>
 * Each property can be a Java type, both simple types and collections are permitted. In addition,
 * {@code ComponentProperties} classes can be composed allowing hierarchies of properties and collections of properties
 * to be reused.
 * <p/>
 * A property is defined using a field in a subclass of this class. Each property field is initialized with one of the
 * following:
 * <ol>
 * <li>For a single property, a {@link Property} object, usually using a static method from the {@link PropertyFactory}.
 * </li>
 * <li>For a reference to other properties, a subclass of {@code ComponentProperties}.</li>
 * <li>For a presentation item that's not actually a property, but is necessary for the user interface, a
 * {@link PresentationItem}.</li>
 * </ol>
 * <p/>
 * For construction of user interfaces, properties are grouped into {@link Form} objects which can be presented in
 * various ways by the user interface (for example, a wizard page, a tab in a property sheet, or a dialog). The same
 * property can appear in multiple forms.
 * <p/>
 * Methods can be added in subclasses according to the conventions below to help direct the UI. These methods will be
 * automatically called by the UI code.
 * <ul>
 * <li>{@code before&lt;PropertyName&gt;} - Called before the property is presented in the UI. This can be used to
 * compute anything required to display the property.</li>
 * <li>{@code after&lt;PropertyName&gt;} - Called after the property is presented and validated in the UI. This can be
 * used to update the properties state to consider the changed in this property.</li>
 * <li>{@code validate&lt;PropertyName&gt;} - Called to validate the property value that has been entered in the UI.
 * This will return a {@link ValidationResult} object with any error information.</li>
 * <li>{@code beforeForm&lt;FormName&gt;} - Called before the form is displayed.</li>
 * </ul>
 * <p/>
 * <b>WARNING</b> - A property shall be created as instance field before the constructor is called so that this abstract
 * constructor can attach i18n translator to the properties. If you want to create the property later you'll have to
 * call {@link SchemaElement#setI18nMessageFormater(I18nMessages)} manually.
 */

public abstract class ComponentProperties extends TranslatableImpl implements SchemaElement, ToStringIndent {

    static final String METHOD_BEFORE = "before";

    static final String METHOD_AFTER = "after";

    static final String METHOD_VALIDATE = "validate";

    // consider removing this in favor of beforeRender at the property level
    static final String METHOD_BEFORE_FORM = "beforeFormPresent";

    static final String METHOD_AFTER_FORM_BACK = "afterFormBack";

    static final String METHOD_AFTER_FORM_NEXT = "afterFormNext";

    static final String METHOD_AFTER_FORM_FINISH = "afterFormFinish";

    // Not a component property
    protected ComponentPropertiesInternal internal;

    /**
     * Name of the special Returns property.
     */
    public static final String RETURNS = "returns";

    /**
     * A special property for the values that a component returns. If this is used, this will be a {@link SchemaElement}
     * that contains each of the values the component returns.
     */
    protected Property returns;

    /**
     * Holder class for the results of a deserialization.
     */
    public static class Deserialized {

        public ComponentProperties properties;

        public MigrationInformation migration;
    }

    // FIXME - will be moved
    public static class MigrationInformationImpl implements MigrationInformation {

        @Override
        public boolean isMigrated() {
            return false;
        }

        @Override
        public String getVersion() {
            return null;
        }
    }

    /**
     * Returns the ComponentProperties object previously serialized.
     *
     * @param serialized created by {@link #toSerialized()}.
     * @return a {@code ComponentProperties} object represented by the {@code serialized} value.
     */
    public static synchronized Deserialized fromSerialized(String serialized) {
        Deserialized d = new Deserialized();
        d.migration = new MigrationInformationImpl();
        // this set the proper classloader for the JsonReader especially for OSGI
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ComponentProperties.class.getClassLoader());
            d.properties = (ComponentProperties) JsonReader.jsonToJava(serialized);
            if (false)
                d.properties.handlePropEncryption(!ENCRYPT);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
        return d;
    }

    /**
     * Do not subclass this method for initialization, use {@link #init()} instead.
     */
    public ComponentProperties(String name) {
        internal = new ComponentPropertiesInternal();
        setName(name);
    }

    /**
     * Initialize this object, all subclass initialization should override this (and call the superclass).
     */
    public ComponentProperties init() {
        // init nested properties starting from the bottom ones
        List<SchemaElement> properties = getProperties();
        for (SchemaElement prop : properties) {
            if (prop instanceof ComponentProperties) {
                ((ComponentProperties) prop).init();
            }
        }
        // initSubclass();
        setupLayout();
        for (Form form : getForms()) {
            refreshLayout(form);
        }
        return this;
    }

    /**
     * Returns a serialized version of this for storage in a repository.
     *
     * @return the serialized {@code String}, use {@link #fromSerialized(String)} to materialize the object.
     */
    public String toSerialized() {
        if (false)
            handlePropEncryption(ENCRYPT);
        String ser = JsonWriter.objectToJson(this);
        // Fix them back
        if (false)
            handlePropEncryption(!ENCRYPT);
        return ser;
    }

    protected static final boolean ENCRYPT = true;

    protected void handlePropEncryption(boolean encrypt) {
        List<SchemaElement> props = getProperties();
        for (SchemaElement se : props) {
            if (se instanceof ComponentProperties) {
                ((ComponentProperties) se).handlePropEncryption(encrypt);
                continue;
            }
            if (se instanceof Property) {
                Property prop = (Property) se;
//                if (prop.isFlag(Property.Flags.ENCRYPT)) {
//                    String value = prop.getStringValue();
//                    CryptoHelper ch = new CryptoHelper(CryptoHelper.PASSPHRASE);
//                    if (encrypt)
//                        prop.setValue(ch.encrypt(value));
//                    else
//                        prop.setValue(ch.decrypt(value));
//                }
            }
        }
    }

    /**
     * Declare the widget information for each of the properties
     */
    protected void setupLayout() {
    }

    /**
     * This is called every time the presentation of the components properties needs to be updated.
     *
     * Note: This is automatically called at startup after all of the setupLayout() calls are done. It only needs to be
     * called after that when the layout has been changed.
     */
    public void refreshLayout(Form form) {
        form.setRefreshUI(true);
    }

    public List<Form> getForms() {
        return internal.getForms();
    }

    public Form getForm(String formName) {
        return internal.getForm(formName);
    }

    public String getSimpleClassName() {
        return getClass().getSimpleName();
    }

    public void addForm(Form form) {
        internal.getForms().add(form);
    }

    public ComponentDesigner getComponentDesigner() {
        return internal.getDesigner();
    }

    public void setComponentDesigner(ComponentDesigner designer) {
        internal.setDesigner(designer);
    }

    /**
     * Returns the list of properties associated with this object.
     * 
     * @return all properties associated with this object (including those defined in superclasses).
     */
    public List<SchemaElement> getProperties() {
        List<SchemaElement> properties = new ArrayList<>();
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            if (SchemaElement.class.isAssignableFrom(f.getType())) {
                try {
                    SchemaElement se = (SchemaElement) f.get(this);
                    if (se != null) {
                        properties.add(se);
                        if (se instanceof Property) {
                            ((Property) se).setComponentProperties(this);
                        }
                        // Do not set the i18N for nested ComponentProperties, they already handle their i18n
                        if (!(se instanceof ComponentProperties)) {
                            se.setI18nMessageFormater(getI18nMessageFormater());
                        }
                    } // else element not initialised (set to null)
                } catch (IllegalAccessException e) {
                    throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                }
            }
        }
        if (returns != null) {
            returns.setI18nMessageFormater(getI18nMessageFormater());
            properties.add(returns);
        }
        return properties;
    }

    public List<String> getPropertyFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            if (!SchemaElement.class.isAssignableFrom(f.getType())) {
                continue;
            }
            fieldNames.add(f.getName());
        }
        return fieldNames;
    }

    /**
     * Returns the property as specified by a qualifed property name string.
     * <p/>
     * The first component is the property name within this object. The optional subsequent components, separated by a
     * "." are property names in the nested {@link ComponentProperties} objects.
     *
     * @param name a qualified property name
     */
    public SchemaElement getProperty(@NotNull String name) {
        String[] propComps = name.split("\\.");
        ComponentProperties currentProps = this;
        int i = 0;
        for (String prop : propComps) {
            if (i++ == propComps.length - 1) {
                return currentProps.getLocalProperty(prop);
            }
            SchemaElement se = currentProps.getLocalProperty(prop);
            if (!(se instanceof ComponentProperties)) {
                throw new IllegalArgumentException(prop + " is not a nested ComponentProperties. Processing: " + name);
            }
            currentProps = (ComponentProperties) currentProps.getLocalProperty(prop);
        }
        return null;
    }

    /**
     * Returns the property in this object specified by a the simple (unqualified) property name.
     * 
     * @param name a simple property name.
     */
    protected SchemaElement getLocalProperty(@NotNull String name) {
        List<SchemaElement> properties = getProperties();
        for (SchemaElement prop : properties) {
            if (name.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    public SchemaElement getPropertyByFieldName(@NotNull String fieldName) {
        SchemaElement prop = null;
        try {
            prop = (SchemaElement) getClass().getField(fieldName).get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            return null;
        }
        return prop;
    }

    public void setValue(SchemaElement property, Object value) {
        if (property.getType() == Type.SCHEMA && value instanceof String)
            value = SchemaFactory.fromSerialized((String) value);

        internal.setValue(property, value);
    }

    public void setValue(String property, Object value) {
        SchemaElement p = getProperty(property);
        if (!(p instanceof Property))
            throw new IllegalArgumentException("setValue but property: " + property + " is not a Property");
        ((Property)p).setValue(value);
    }

    public Object getValue(SchemaElement property) {
        return internal.getValue(property);
    }

    public boolean getBooleanValue(SchemaElement property) {
        Boolean value = (Boolean) getValue(property);
        return value != null && value;
    }

    public String getStringValue(SchemaElement property) {
        Object value = getValue(property);
        if (value != null) {
            if (value instanceof Schema) {
                return ((Schema) value).toSerialized();
            }
            return value.toString();
        }
        return null;
    }

    public int getIntValue(SchemaElement property) {
        Integer value = (Integer) getValue(property);
        if (value == null) {
            return 0;
        }
        return value;
    }

    public Calendar getCalendarValue(SchemaElement property) {
        return (Calendar) getValue(property);
    }

    /**
     * Returns the {@link ValidationResult} for the property being validated if requested.
     *
     * @return a ValidationResult
     */
    public ValidationResult getValidationResult() {
        return internal.getValidationResult();
    }

    /**
     * Copy all of the values from the specified {@link ComponentProperties} object. This includes the values from any
     * nested objects.
     * 
     * @param props
     */
    public void copyValuesFrom(ComponentProperties props) {
        List<SchemaElement> values = getProperties();
        for (SchemaElement se : values) {
            SchemaElement otherSe = props.getProperty(se.getName());
            if (otherSe == null)
                continue;
            if (se instanceof ComponentProperties) {
                ((ComponentProperties) se).copyValuesFrom((ComponentProperties) otherSe);
            } else {
                Object value = props.getValue(otherSe);
                setValue(se, value);
            }
        }

    }

    // Internal - not API
    public void setWidgetLayoutMethods(String property, Widget widget) {
        Method m;
        m = findMethod(METHOD_BEFORE, property, !REQUIRED);
        if (m != null) {
            widget.setCallBefore(true);
        }
        m = findMethod(METHOD_AFTER, property, !REQUIRED);
        if (m != null) {
            widget.setCallAfter(true);
        }
        m = findMethod(METHOD_VALIDATE, property, !REQUIRED);
        if (m != null) {
            widget.setCallValidate(true);
        }
    }

    // Internal - not API
    public void setFormLayoutMethods(String property, Form form) {
        Method m;
        m = findMethod(METHOD_BEFORE_FORM, property, !REQUIRED);
        if (m != null) {
            form.setCallBeforeFormPresent(true);
        }
        m = findMethod(METHOD_AFTER_FORM_BACK, property, !REQUIRED);
        if (m != null) {
            form.setCallAfterFormBack(true);
        }
        m = findMethod(METHOD_AFTER_FORM_NEXT, property, !REQUIRED);
        if (m != null) {
            form.setCallAfterFormNext(true);
        }
        m = findMethod(METHOD_AFTER_FORM_FINISH, property, !REQUIRED);
        if (m != null) {
            form.setCallAfterFormFinish(true);
        }
    }

    private boolean REQUIRED = true;

    Method findMethod(String type, String propName, boolean required) {
        if (propName == null) {
            throw new IllegalArgumentException(
                    "The ComponentService was used to access a property with a null property name. Type: " + type
                            + " Properties: " + this);
        }
        propName = propName.substring(0, 1).toUpperCase() + propName.substring(1);
        String methodName = type + propName;
        Method[] methods = getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        if (required) {
            throw new IllegalStateException("Method: " + methodName + " not found");
        }
        return null;
    }

    private void doInvoke(Method m) throws Throwable {
        try {
            m.invoke(this);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public void validateProperty(String propName) throws Throwable {
        Method m = findMethod(METHOD_VALIDATE, propName, REQUIRED);
        try {
            ValidationResult validationResult = (ValidationResult) m.invoke(this);
            internal.setValidationResult(validationResult);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public void beforePropertyActivate(String propName) throws Throwable {
        doInvoke(findMethod(METHOD_BEFORE, propName, REQUIRED));
    }

    public void beforePropertyPresent(String propName) throws Throwable {
        doInvoke(findMethod(METHOD_BEFORE, propName, REQUIRED));
    }

    public void afterProperty(String propName) throws Throwable {
        doInvoke(findMethod(METHOD_AFTER, propName, REQUIRED));
    }

    public void beforeFormPresent(String formName) throws Throwable {
        doInvoke(findMethod(METHOD_BEFORE_FORM, formName, REQUIRED));
    }

    public void afterFormNext(String formName) throws Throwable {
        doInvoke(findMethod(METHOD_AFTER_FORM_NEXT, formName, REQUIRED));
    }

    public void afterFormBack(String formName) throws Throwable {
        doInvoke(findMethod(METHOD_AFTER_FORM_BACK, formName, REQUIRED));
    }

    public void afterFormFinish(String formName) throws Throwable {
        doInvoke(findMethod(METHOD_AFTER_FORM_FINISH, formName, REQUIRED));
    }

    /*
     * Implementation required because we are a SchemaElement
     */

    @Override
    public String getName() {
        return internal.getName();
    }

    @Override
    public SchemaElement setName(String name) {
        internal.setName(name);
        return this;
    }

    @Override
    public String getDisplayName() {
        return getI18nMessage("properties" + (getName() == null ? getName() : "") + ".displayName");
    }

    public SchemaElement setDisplayName(String displayName) {
        // FIXME - need better exception for this
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public String getTitle() {
        return internal.getTitle();
    }

    @Override
    public SchemaElement setTitle(String title) {
        internal.setTitle(title);
        return this;
    }

    @Override
    public Type getType() {
        return Type.GROUP;
    }

    @Override
    public SchemaElement setType(Type type) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public SchemaElement setSize(int size) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public boolean isSizeUnbounded() {
        return true;
    }

    @Override
    public int getOccurMinTimes() {
        return 1;
    }

    @Override
    public SchemaElement setOccurMinTimes(int times) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public int getOccurMaxTimes() {
        return 1;
    }

    @Override
    public SchemaElement setOccurMaxTimes(int times) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public SchemaElement setRequired() {
        return setRequired(true);
    }

    @Override
    public SchemaElement setRequired(boolean required) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public int getPrecision() {
        return 0;
    }

    @Override
    public SchemaElement setPrecision(int precision) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public String getPattern() {
        return null;
    }

    @Override
    public SchemaElement setPattern(String pattern) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public SchemaElement setDefaultValue(String defaultValue) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public SchemaElement setNullable(boolean nullable) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public Class<?> getEnumClass() {
        return null;
    }

    @Override
    public SchemaElement setEnumClass(Class<?> enumClass) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public List<?> getPossibleValues() {
        return null;
    }

    @Override
    public SchemaElement setPossibleValues(List<?> possibleValues) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public SchemaElement setPossibleValues(Object... values) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public List<SchemaElement> getChildren() {
        return getProperties();
    }

    @Override
    public SchemaElement setChildren(List<SchemaElement> children) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public SchemaElement getChild(String name) {
        return null;
    }

    @Override
    public SchemaElement addChild(SchemaElement child) {
        throw new RuntimeException("Cannot be used here");
    }

    @Override
    public Map<String, SchemaElement> getChildMap() {
        Map<String, SchemaElement> map = new HashMap<>();
        for (SchemaElement se : getChildren()) {
            map.put(se.getName(), se);
        }
        return map;
    }

    @Override
    public String toString() {
        return toStringIndent(0);
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder sb = new StringBuilder();
        String is = ToStringIndentUtil.indentString(indent);
        sb.append(is + getName() + " - " + getTitle() + " " + getClass().getName());
        sb.append("\n" + is + "   Properties:");
        for (SchemaElement prop : getProperties()) {
            sb.append("\n" + prop.toStringIndent(indent + 6));
            String value = getStringValue(prop);
            if (value != null) {
                sb.append(" [" + value + "]");
            }
        }
        sb.append("\n " + is + "  Forms:");
        for (Form form : getForms()) {
            sb.append("\n" + form.toStringIndent(indent + 6));
        }
        return sb.toString();
    }

}
