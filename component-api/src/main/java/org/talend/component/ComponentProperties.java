package org.talend.component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 * TODO - need to serialize the annotations associated with each property so the client can see them.
 * @author fupton
 *
 */

//@JsonSerialize(using = ComponentPropertiesSerializer.class)
public abstract class ComponentProperties {

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
	
	@JsonAnyGetter
	public Map<String, Object> getAnnotations() {
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

}
