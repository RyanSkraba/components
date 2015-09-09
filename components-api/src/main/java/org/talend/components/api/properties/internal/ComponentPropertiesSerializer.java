package org.talend.components.api.properties.internal;

import java.io.IOException;

import org.talend.components.api.properties.ComponentProperties;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

class ComponentPropertiesSerializer extends JsonSerializer<ComponentProperties> {

    @Override
    public void serialize(ComponentProperties cp, JsonGenerator jsonGenerator, SerializerProvider sp) throws IOException {

        // FIXME

    }

}
