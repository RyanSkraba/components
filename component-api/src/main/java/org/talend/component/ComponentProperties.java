package org.talend.component;

import org.talend.component.properties.Property;
import org.talend.component.properties.layout.Layout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The {@code ComponentProperties} class contains the definitions of the properties associated
 * with a component. These definitions contain enough information to automatically
 * construct a nice looking user interface (UI) to populate and validate the properties. The objective
 * is that no actual (graphical) UI code is included in the component's definition
 * and as well no custom graphical UI is required for most components. The types of UIs that
 * can be defined include those for desktop (Eclipse), web, and scripting. All of these
 * will use the code defined here for their construction and validation.
 * <p>
 * All aspects of the properties are defined in a subclass of this
 * class using the {@link Property} and {@link Layout} classes. In addition in cases where
 * user interface decisions are made in code, methods can be added to the subclass
 * to influence the flow of the user interface and help with validation.
 * <p>
 * Each property can be a Java type, both simple types and collections are permitted.
 * In addition, {@code ComponentProperties} classes can be composed allowing hierarchies
 * of properties and collections of properties to be reused.
 * <p>
 * Properties can be grouped into forms which can be presented in various ways
 * by the user interface (for example, a wizard page, a tab in a property sheet, or a dialog).
 * A wizard can be defined which is a sequence of forms.
 *
 */

// @JsonSerialize(using = ComponentPropertiesSerializer.class)
public abstract class ComponentProperties {

    public static final String PAGE_MAIN = "main";

    public static final String PAGE_ADVANCED = "advanced";

    /**
     * This is called every time the layout of the component properties needs to be updated
     */
    public void refreshLayout() {
        // do nothing by default
    }

    /**
     * Component properties, can we have each property assocaited with multiple pages? There should be a default page
     * which is the standard property sheet (if no other page is associated).
     * <p>
     * Split this into component properties which is the actual container, and ComponentPropertiesDesigner which
     * contains the instructions for the designer (which might be updated by the component service).
     * <p>
     * Also, abstract schema handling stuff from this.
     */

    ComponentProperties validateProperty(String propName, String value) {
        // Need to see if there is a method in the component properties that handles this validation and dispatch it.
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
