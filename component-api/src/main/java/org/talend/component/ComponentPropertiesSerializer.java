package org.talend.component;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

class ComponentPropertiesSerializer extends JsonSerializer<ComponentProperties> {

	@Override
	public void serialize(ComponentProperties cp, JsonGenerator jsonGenerator,
			SerializerProvider sp) throws IOException, JsonProcessingException {

		// FIXME

	}

}
