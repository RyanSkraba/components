package org.talend.components.base;

import org.talend.components.base.internal.ComponentPropertiesInternal;
import org.talend.components.base.properties.Property;
import org.talend.components.base.properties.ValidationResult;
import org.talend.components.base.properties.presentation.Form;
import org.talend.components.base.properties.presentation.Layout;
import org.talend.components.base.properties.presentation.Wizard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ComponentProperties} class contains the definitions of the layoutMap associated
 * with a components. These definitions contain enough information to automatically
 * construct a nice looking user interface (UI) to populate and validate the layoutMap. The objective
 * is that no actual (graphical) UI code is included in the components's definition
 * and as well no custom graphical UI is required for most components. The types of UIs that
 * can be defined include those for desktop (Eclipse), web, and scripting. All of these
 * will use the code defined here for their construction and validation.
 * <p>
 * All aspects of the layoutMap are defined in a subclass of this
 * class using the {@link Property}, {@link Layout}, {@link Form}, and {@link Wizard} classes.
 * In addition in cases where user interface decisions are made in code, methods can be added to the subclass
 * to influence the flow of the user interface and help with validation.
 * <p>
 * Each property can be a Java type, both simple types and collections are permitted.
 * In addition, {@code ComponentProperties} classes can be composed allowing hierarchies
 * of layoutMap and collections of layoutMap to be reused.
 * <p>
 * Properties can be grouped into {@link Form} objects
 * which can be presented in various ways by the user interface (for example, a wizard page, a
 * tab in a property sheet, or a dialog).
 * <p>
 * A {@link Wizard} can be defined which is a sequence of forms.
 * <p>
 * Methods can be added in subclasses according to the conventions below to help direct the UI. These methods
 * will be automatically called by the UI code.
 * <ul>
 * <li>{@code before&lt;PropertyName&gt;} - Called before the property is presented in the UI. This
 * can be used to compute anything required to display the property.
 * </li>
 * <li>{@code validate&lt;PropertyName&gt;} - Called after the property value has been entered in the UI.
 * This will return a {@link ValidationResult} object with any error information.
 * </li>
 * <li>{@code beforeForm&lt;FormName&gt;} - Called before the form is displayed.
 * </li>
 * </ul>
 */

// @JsonSerialize(using = ComponentPropertiesSerializer.class)
public abstract class ComponentProperties {

    // Not a component property
    protected ComponentPropertiesInternal internal;

    public Property<String> name = new Property<String>("name", "Name");

    public Property<String> description = new Property<String>("description", "Descripton");

    public ComponentProperties() {
        internal = new ComponentPropertiesInternal();
    }

    public List<Form> getForms() {
        return internal.getForms();
    }

    public Form getForm(String formName) {
        return internal.getForm(formName);
    }

    public List<Wizard> getWizards() {
        return internal.getWizards();
    }

    public void addForm(Form form) {
        internal.getForms().add(form);
    }

    public void addWizard(Wizard wizard) {
        internal.getWizards().add(wizard);
    }

    // TODO - do we need something indicating the form is to be refreshed. This should be at the Form level.

    /**
     * Declare the layout information for each of the layoutMap
     */
    protected void setupLayout() {
    }

    /**
     * This is called every time the presentation of the components layoutMap needs to be updated
     */
    public void refreshLayout() {
        // do nothing by default
    }

    /**
     * Component layoutMap, can we have each property assocaited with multiple pages? There should be a default page
     * which is the standard property sheet (if no other page is associated).
     * <p>
     * Split this into components layoutMap which is the actual container, and ComponentPropertiesDesigner which
     * contains the instructions for the designer (which might be updated by the components service).
     * <p>
     * Also, abstract schema handling stuff from this.
     */

    ComponentProperties validateProperty(String propName, String value) {
        // Need to see if there is a method in the components layoutMap that handles this validation and dispatch it.
        propName = propName.substring(0, 1).toUpperCase() + propName.substring(1);
        Method[] methods = getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().endsWith(propName)) {
                try {

                    // TODO: Convert the value to the correct type

                    m.invoke(this, value);
                    break;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

}
