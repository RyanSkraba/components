package org.talend.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author fupton
 */

// @JsonSerialize(using = ComponentPropertiesSerializer.class)
public abstract class ComponentProperties {

    public static final String PAGE_MAIN = "main";

    public static final String PAGE_ADVANCED = "advanced";

    /**
     * Unique identification of the this object for the lifetime of the ComponentService. Used to reference the current
     * value of the ComponentProperties on the server.
     */
    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * This is called everytime the layout of the component properties needs to be updated
     */
    public void refreshLayout() {
        // do nothing by default
    }

    /**
     * Component properties, can we have each property assocaited with multiple pages? There should be a default page
     * which is the standard property sheet (if no other page is associated).
     *
     * Split this into component properties which is the actual container, and ComponentPropertiesDesigner which
     * contains the instructions for the designer (which might be updated by the component service).
     *
     * Also, abstract schema handling stuff from this.
     *
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
