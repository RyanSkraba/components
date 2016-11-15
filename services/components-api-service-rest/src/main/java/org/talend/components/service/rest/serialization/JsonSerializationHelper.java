// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.service.rest.serialization;

import java.io.InputStream;
import java.util.HashMap;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * this provide helper methods from serializing and deserializing TComp object for REST apis
 */
@Component
public class JsonSerializationHelper {

    @Inject
    DefinitionRegistryService definitionRegistry;

    private HashMap<String, Object> jsonIoOptions;

    public JsonSerializationHelper() {
        jsonIoOptions = new HashMap<>();
        jsonIoOptions.put(JsonWriter.TYPE, false);
    }

    /**
     * Setup {@link Properties} from the data contained in the json-data stream (UTF-8) into a
     * 
     * @param jsonDataStream json-data formated input stream in UTF-8
     * @param initializedProperties instance of the properties to iniitalize with the json data.
     * @return the same instance as initalisedProperties setup with the json-data values.
     */
    public <P extends Properties> P toProperties(InputStream jsonDataStream, P initializedProperties) {
        return JsonSchemaUtil.fromJson(jsonDataStream, initializedProperties);
    }

    /**
     * Setup {@link Properties} from the data contained in the json-data stream (UTF-8) into a
     * 
     * @param jsonDataStream json-data formated input stream in UTF-8
     * @return a properties instance, never null.
     */
    public Properties toProperties(InputStream jsonDataStream) {
        return JsonSchemaUtil.fromJson(jsonDataStream, definitionRegistry);
    }

    /**
     * Creates a ui-spec representation of the properties including json-schema, json-ui and json-data
     * 
     * @param properties instance of the properties to serialize.
     * @return json string in ui-specs representation of the data.
     */
    public String toJson(Properties properties, String definitionName) {
        return JsonSchemaUtil.toJson(properties, definitionName);
    }

    public String toJson(DatastoreDefinition definition) {
        return toJson((Object) definition);
    }

    /**
     * convert jsonStream (Jsonio with no type) input stream into the given clazz
     */
    public <T> T toObject(InputStream jsonStream, Class<T> clazz) {
        return SerializerDeserializer.fromSerialized(jsonStream, clazz, null, false).object;
    }

    /**
     * convert obj into a json String (using Jsonio with no @type)
     */
    public String toJson(Object obj) {
        return SerializerDeserializer.toSerialized(obj, false, jsonIoOptions);
    }

}
