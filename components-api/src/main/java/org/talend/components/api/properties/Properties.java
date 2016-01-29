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
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.talend.components.api.ComponentDesigner;
import org.talend.components.api.NamedThing;
import org.talend.components.api.ToStringIndent;
import org.talend.components.api.ToStringIndentUtil;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.i18n.TranslatableImpl;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.SchemaElement;
import org.talend.daikon.exception.ExceptionContext;
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

public abstract class Properties extends TranslatableImpl implements AnyProperty, ToStringIndent {

    static final String METHOD_BEFORE = "before";

    static final String METHOD_AFTER = "after";

    static final String METHOD_VALIDATE = "validate";

    // consider removing this in favor of beforeRender at the property level
    static final String METHOD_BEFORE_FORM = "beforeFormPresent";

    static final String METHOD_AFTER_FORM_BACK = "afterFormBack";

    static final String METHOD_AFTER_FORM_NEXT = "afterFormNext";

    static final String METHOD_AFTER_FORM_FINISH = "afterFormFinish";

    private String name;

    private ComponentDesigner designer;

    private boolean runtimeOnly;

    private List<Form> forms = new ArrayList<>();

    private ValidationResult validationResult;

    /**
     * Name of the special Returns property.
     */
    public static final String RETURNS = "returns";

    /**
     * A special property for the values that a component returns. If this is used, this will be a {@link SchemaElement}
     * that contains each of the values the component returns.
     */
    public Property returns;

    /**
     * Holder class for the results of a deserialization.
     */
    public static class Deserialized {

        public Properties properties;

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
            Thread.currentThread().setContextClassLoader(Properties.class.getClassLoader());
            d.properties = (Properties) JsonReader.jsonToJava(serialized);
            d.properties.handlePropEncryption(!ENCRYPT);
            d.properties.setupPropertiesPostDeserialization();
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
        return d;
    }

    /**
     * This will setup all ComponentProperties after the deserialization process. For now it will just setup i18N
     */
    private void setupPropertiesPostDeserialization() {
        initLayout();
        List<NamedThing> properties = getProperties();
        for (NamedThing prop : properties) {
            if (prop instanceof Properties) {
                ((Properties) prop).setupPropertiesPostDeserialization();
            } else {
                prop.setI18nMessageFormater(getI18nMessageFormater());
            }
        }

    }

    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     * 
     * @param name, uniquely identify the property among other properties when used as nested properties.
     */
    public Properties(String name) {
        setName(name);
    }

    /**
     * Must be called once the class is instanciated to setup the properties and the layout
     * 
     * @return this instance
     */
    public Properties init() {
        // init nested properties starting from the bottom ones
        initProperties();
        initLayout();
        return this;
    }

    /**
     * only initilize the properties but not the layout.
     * 
     * @return this instance
     */
    public Properties initForRuntime() {
        initProperties();
        return this;
    }

    private void initProperties() {
        List<Field> uninitializedProperties = new ArrayList<>();
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            try {
                if (isAPropertyType(f.getType())) {
                    NamedThing se = (NamedThing) f.get(this);
                    if (se != null) {
                        initializeField(f, se);
                    } else {// not yet initialized to record it
                        uninitializedProperties.add(f);
                    }
                } // else not a field that ought to be initialized
            } catch (IllegalAccessException e) {
                throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
        setupProperties();
        // initialize all the properties that where found and not initialized
        // they must be initalized after the setup.
        for (Field f : uninitializedProperties) {
            NamedThing se;
            try {
                se = (NamedThing) f.get(this);
                if (se != null) {
                    initializeField(f, se);
                } else {// field not initilaized but is should be (except for returns field)
                    if (!RETURNS.equals(f.getName())) {
                        throw new ComponentException(ComponentsErrorCode.COMPONENT_HAS_UNITIALIZED_PROPS, ExceptionContext
                                .withBuilder().put("name", this.getClass().getCanonicalName()).put("field", f.getName()).build());
                    } // else a returns field that may not be initialized
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
    }

    /**
     * This shall set the value holder for all the properties, set the i18n formatter of this current class to the
     * properties so that the i18n values are computed agains this class message properties. This calls the
     * initProperties for all field of type ComponentProperties
     * 
     * @param f field to be initialized
     * @param value associated with this field, never null
     */
    public void initializeField(Field f, NamedThing value) {
        // check that field name matches the NamedThing name
        if (!f.getName().equals(value.getName())) {
            throw new IllegalArgumentException("The java field [" + this.getClass().getCanonicalName() + "." + f.getName()
                    + "] should be named identically to the instance name [" + value.getName() + "]");
        }
        if (value instanceof Property) {
            // Do not set the i18N for nested ComponentProperties, they already handle their i18n
            value.setI18nMessageFormater(getI18nMessageFormater());
        } else {// a Component property so setit up
            ((Properties) value).initProperties();
        }
    }

    private void initLayout() {
        List<NamedThing> properties = getProperties();
        for (NamedThing prop : properties) {
            if (prop instanceof Properties) {
                ((Properties) prop).initLayout();
            }
        }
        setupLayout();
        for (Form form : getForms()) {
            refreshLayout(form);
        }
    }

    /**
     * Initialize this object, all subclass initialization should override this, and call the super. <br>
     * WARNING : make sure to call super() first otherwise you may endup with NPE because of not initialised properties
     */
    public void setupProperties() {
    }

    /**
     * Declare the widget layout information for each of the properties.<br>
     * WARNING : make sure to call super() first otherwise you may endup with NPE because of not initialised layout
     */
    public void setupLayout() {
    }

    /**
     * Returns a serialized version of this for storage in a repository.
     *
     * @return the serialized {@code String}, use {@link #fromSerialized(String)} to materialize the object.
     */
    public String toSerialized() {
        handlePropEncryption(ENCRYPT);
        List<Form> currentForms = forms;
        String ser = null;
        try {
            // The forms are recreated upon deserialization
            forms = new ArrayList();
            ser = JsonWriter.objectToJson(this);
        } finally {
            forms = currentForms;
        }
        handlePropEncryption(!ENCRYPT);
        return ser;

    }

    protected static final boolean ENCRYPT = true;

    protected void handlePropEncryption(final boolean encrypt) {
        accept(new AnyPropertyVisitor() {

            @Override
            public void visit(Properties properties) {
                // nothing to be encrypted here
            }

            @Override
            public void visit(Property property) {
                if (property.isFlag(Property.Flags.ENCRYPT)) {
                    String value = (String) property.getStoredValue();
                    CryptoHelper ch = new CryptoHelper(CryptoHelper.PASSPHRASE);
                    if (encrypt) {
                        property.setValue(ch.encrypt(value));
                    } else {
                        property.setValue(ch.decrypt(value));
                    }
                }
            }
        });
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
        return forms;
    }

    public Form getForm(String formName) {
        for (Form f : forms) {
            if (f.getName().equals(formName)) {
                return f;
            }
        }
        return null;
    }

    public String getSimpleClassName() {
        return getClass().getSimpleName();
    }

    public void addForm(Form form) {
        forms.add(form);
    }

    public ComponentDesigner getComponentDesigner() {
        return designer;
    }

    public void setComponentDesigner(ComponentDesigner designer) {
        this.designer = designer;
    }

    /**
     * Returns the list of properties associated with this object.
     * 
     * @return all properties associated with this object (including those defined in superclasses).
     */
    public List<NamedThing> getProperties() {
        // TODO this should be changed to AnyProperty type but it as impact everywhere
        List<NamedThing> properties = new ArrayList<>();
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            try {
                Object fValue = f.get(this);
                if (isAPropertyType(f.getType())) {
                    if (fValue != null) {
                        NamedThing se = (NamedThing) fValue;
                        properties.add(se);
                    } // else not initalized but this is already handled in the initProperties that must be called
                      // before the getProperties
                }
            } catch (IllegalAccessException e) {
                throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
        return properties;
    }

    @Override
    public void accept(AnyPropertyVisitor visitor) {
        List<NamedThing> properties = getProperties();
        for (NamedThing nt : properties) {
            if (nt instanceof AnyProperty) {
                ((AnyProperty) nt).accept(visitor);
            }
        }
        visitor.visit(this);
    }

    /**
     * is this object of type Property or ComponenetProperties, the properties type handle by this class.
     * 
     * @param clazz, the class to be tested
     * @return true if the clazz inherites from Property or ComponenetProperties
     */
    protected boolean isAPropertyType(Class<?> clazz) {
        return Properties.class.isAssignableFrom(clazz) || Property.class.isAssignableFrom(clazz);
    }

    /**
     * Returns Property or a CompoentProperties as specified by a qualifed property name string representing the field
     * name.
     * <p/>
     * The first component is the property name within this object. The optional subsequent components, separated by a
     * "." are property names in the nested {@link Properties} objects.
     *
     * @param name a qualified property name
     * @return the Property or Componenent denoted with the name or null if the final field is not found
     * @exception IllegalArgumentException is the path before the last does not point to a CompoenentProperties
     */
    public NamedThing getProperty(@NotNull String name) {
        // TODO make the same behaviour if the nested ComponentProperties name is not found or the last properties is
        // not found
        // cause right now if the ComponentProperties is not foudnt an execpetion is thrown and if the last property is
        // not found null is returned.
        String[] propComps = name.split("\\.");
        Properties currentProps = this;
        int i = 0;
        for (String prop : propComps) {
            if (i++ == propComps.length - 1) {
                return currentProps.getLocalProperty(prop);
            }
            NamedThing se = currentProps.getLocalProperty(prop);
            if (!(se instanceof Properties)) {
                throw new IllegalArgumentException(prop + " is not a nested ComponentProperties. Processing: " + name);
            } // else se is a CompoenetProperties so use it
            currentProps = (Properties) se;
        }
        return null;
    }

    /**
     * same as {@link Properties#getProperties()} but returns null if the Property is not of type Property.
     */
    public Property getValuedProperty(String propPath) {
        NamedThing prop = getProperty(propPath);
        return (prop instanceof Property) ? (Property) prop : null;
    }

    /**
     * same as {@link Properties#getProperties()} but returns null if the Property is not of type ComponentProperty.
     */
    public Properties getComponentProperties(String propPath) {
        NamedThing prop = getProperty(propPath);
        return (prop instanceof Properties) ? (Properties) prop : null;
    }

    /**
     * Returns the property in this object specified by a the simple (unqualified) property name.
     * 
     * @param name a simple property name.
     */
    protected NamedThing getLocalProperty(@NotNull String name) {
        List<NamedThing> properties = getProperties();
        for (NamedThing prop : properties) {
            if (name.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    public void setValue(String property, Object value) {
        NamedThing p = getProperty(property);
        if (!(p instanceof Property)) {
            throw new IllegalArgumentException("setValue but property: " + property + " is not a Property");
        }
        ((Property) p).setValue(value);
    }

    /**
     * Helper method to set the evaluator to all properties handled by this instance and all the nested
     * ComponentProperties instances.
     * 
     * @param ve value evalurator to be used for evaluation.
     */
    public void setValueEvaluator(PropertyValueEvaluator ve) {
        List<NamedThing> properties = getProperties();
        for (NamedThing prop : properties) {
            if (prop instanceof Property) {
                ((Property) prop).setValueEvaluator(ve);
            } else if (prop instanceof Properties) {
                ((Properties) prop).setValueEvaluator(ve);
            }
        }
    }

    /**
     * Returns the {@link ValidationResult} for the property being validated if requested.
     *
     * @return a ValidationResult
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Copy all of the values from the specified {@link Properties} object. This includes the values from any nested
     * objects.
     * 
     * @param props
     */
    public void copyValuesFrom(Properties props) {
        List<NamedThing> values = getProperties();
        for (NamedThing se : values) {
            NamedThing otherSe = props.getProperty(se.getName());
            if (otherSe == null) {
                continue;
            }
            if (se instanceof Properties) {
                ((Properties) se).copyValuesFrom((Properties) otherSe);
            } else {
                Object value = ((Property) otherSe).getStoredValue();
                ((Property) se).setValue(value);
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
            Object result = m.invoke(this);
            if (result instanceof ValidationResult && result != null) {
                validationResult = (ValidationResult) result;
            } else {
                validationResult = ValidationResult.OK;
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public void validateProperty(String propName) throws Throwable {
        Method m = findMethod(METHOD_VALIDATE, propName, REQUIRED);
        try {
            validationResult = (ValidationResult) m.invoke(this);
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
        return name;
    }

    public Properties setName(String name) {

        this.name = name;
        return this;
    }

    @Override
    public String getDisplayName() {
        return getI18nMessage("properties" + (getName() == null ? getName() : "") + ".displayName");
    }

    @Override
    public String getTitle() {
        return name;
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
        for (NamedThing prop : getProperties()) {
            if (prop instanceof ToStringIndent) {
                sb.append('\n' + ((ToStringIndent) prop).toStringIndent(indent + 6));
            } else {
                sb.append('\n' + prop.toString());
            }
            String value = prop instanceof Property ? ((Property) prop).getStringValue() : null;
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
