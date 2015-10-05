package org.talend.components.api.properties;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.talend.components.api.ComponentDesigner;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.i18n.TranslatableImpl;
import org.talend.components.api.properties.internal.ComponentPropertiesInternal;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.SchemaElement;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.i18n.I18nMessages;

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
 * All aspects of the properties are defined in a subclass of this class using the {@link SchemaElement}, {@Link
 * PresentationItem}, {@link Widget}, and {@link Form} classes. In addition in cases where user interface decisions are
 * made in code, methods can be added to the subclass to influence the flow of the user interface and help with
 * validation.
 * <p/>
 * Each property can be a Java type, both simple types and collections are permitted. In addition,
 * {@code ComponentProperties} classes can be composed allowing hierarchies of properties and collections of properties
 * to be reused.
 * <p/>
 * Properties are be grouped into {@link Form} objects which can be presented in various ways by the user interface (for
 * example, a wizard page, a tab in a property sheet, or a dialog). The same property can appear in multiple forms.
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
 * 
 * WARNING : property shall be created as instance field before the constructor is called so that this abstract
 * constructor can attach i18n translator to the properties. If you want to create the property later you'll have to
 * call {@link SchemaElement#setI18nMessageFormater(I18nMessages)} manually.
 */

// @JsonSerialize(using = ComponentPropertiesSerializer.class)
public abstract class ComponentProperties extends TranslatableImpl implements SchemaElement {

    static final String METHOD_BEFORE = "before";

    static final String METHOD_AFTER = "after";

    static final String METHOD_VALIDATE = "validate";

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
    protected SchemaElement returns;

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
    public static Deserialized fromSerialized(String serialized) {
        Deserialized d = new Deserialized();
        d.migration = new MigrationInformationImpl();
        d.properties = (ComponentProperties) JsonReader.jsonToJava(serialized);
        return d;
    }

    public ComponentProperties() {
        internal = new ComponentPropertiesInternal();
    }

    public ComponentProperties(String name) {
        internal = new ComponentPropertiesInternal();
        setName(name);
    }

    // /**
    // * This will use the current I18nMessage to the property handles by this class, but only for direct properties and
    // * not nested ComponentProperties
    // */
    // protected void setupPropertiesWithI18n() {
    // List<SchemaElement> properties = getProperties();
    // for (SchemaElement prop : properties) {
    // if (!(prop instanceof ComponentProperties)) {
    // if (prop != null) {
    // prop.setI18nMessageFormater(i18nMessages);
    // } // else the property has not been initialised yet, please make sure to call this after initilisation
    // } // else this is handle by the constructor of this class.
    // }
    //
    // }

    /**
     * Returns a serialized version of this for storage in a repository.
     *
     * @return the serialized {@code String}, use {@link #fromSerialized(String)} to materialize the object.
     */
    public String toSerialized() {
        return JsonWriter.objectToJson(this);
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

    // FIXME - this does not consider properties in a superclass. Not sure if it should or not, however
    // we need to document that we can't use subclasses to add additional properties from the parent
    // class - we can only extent by composition, not inheritance.
    public List<SchemaElement> getProperties() {
        List<SchemaElement> properties = new ArrayList();
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            if (SchemaElement.class.isAssignableFrom(f.getType())) {
                try {
                    SchemaElement se = (SchemaElement) f.get(this);
                    if (se != null) {
                        properties.add(se);
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

    public SchemaElement getProperty(@NotNull String name) {
        List<SchemaElement> properties = getProperties();
        for (SchemaElement prop : properties) {
            if (name.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    public void setValue(SchemaElement property, Object value) {
        internal.setValue(property, value);
    }

    public Object getValue(SchemaElement property) {
        return internal.getValue(property);
    }

    public boolean getBooleanValue(SchemaElement property) {
        Boolean value = (Boolean) getValue(property);
        if (value == null || !value) {
            return false;
        }
        return true;
    }

    public String getStringValue(SchemaElement property) {
        return (String) getValue(property);
    }

    public int getIntValue(SchemaElement property) {
        Integer value = (Integer) getValue(property);
        if (value == null) {
            return 0;
        }
        return value;
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
     * Declare the widget information for each of the properties
     */
    protected void setupLayout() {
    }

    /**
     * This is called every time the presentation of the components properties needs to be updated
     */
    public void refreshLayout(Form form) {
        form.setRefreshUI(true);
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

    public void beforeProperty(String propName) throws Throwable {
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
    public Class getEnumClass() {
        return null;
    }

    @Override
    public SchemaElement setEnumClass(Class enumClass) {
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
        Map<String, SchemaElement> map = new HashMap();
        for (SchemaElement se : getChildren()) {
            map.put(se.getName(), se);
        }
        return map;
    }

}
