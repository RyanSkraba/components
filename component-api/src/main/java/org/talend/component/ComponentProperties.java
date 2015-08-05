package org.talend.component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 *
 * @author fupton
 */

//@JsonSerialize(using = ComponentPropertiesSerializer.class)
public abstract class ComponentProperties {

    public static final String PAGE_MAIN = "main";

    public static final String PAGE_ADVANCED = "advanced";

    /**
     * Unique identification of the this object for the lifetime of the
     * ComponentService. Used to reference the current value of the
     * ComponentProperties on the server.
     */
    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getProperties(String page) {
        return null;
    }

    /**
     * Component properties, can we have each property assocaited with multiple pages?
     * There should be a default page which is the standard property sheet (if no other page is associated).
     *
     * Split this into component properties which is the actual container, and ComponentPropertiesDesigner which contains
     * the instructions for the designer (which might be updated by the component service).
     *
     * Also, abstract schema handling stuff from this.
     *
     */

    /**
     * This is a temporary way to do this, we probably want to do something based on the properties so that we can have the properties be the root in JSON and everything that applies to them be under them. Including the current value of the property.
     * <p>
     * ADD: we also need to add something that indicates the property is to be checked before focus and after focus. This is triggered by the existence of the validate methods in the actual ComponentProperties implementation.
     *
     * @return
     */
    @JsonAnyGetter public Map<String, Object> getAnnotations() {
        Map<String, Map<String, String>> methods = new HashMap<String, Map<String, String>>();
        Field[] fields = getClass().getFields();
        for (Field field : fields) {
            Map<String, String> annotations = new HashMap();
            for (Annotation an : field.getAnnotations()) {
                // FIXME - pull the value out of here
                annotations.put(an.annotationType().getName(), an.toString());
            }
            methods.put(field.getName(), annotations);
        }
        Map<String, Object> returnMap = new HashMap();
        returnMap.put("_annotations", methods);
        return returnMap;
    }

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
