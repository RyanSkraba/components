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
package org.talend.components.service.rest;

import java.io.InputStream;
import java.util.HashMap;

import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * this provide helper methods from serilizing and deserializing TComp object for REST apis
 */
/**
 *
 */
public class JsonSerializationHelper {

    private HashMap<String, Object> jsonIoOptions;

    public JsonSerializationHelper() {
        jsonIoOptions = new HashMap<>();
        jsonIoOptions.put(JsonWriter.TYPE, false);
    }

    /**
     * Setup {@link Properties} from the data contained in the json-data stream (UTF-8) into a
     * 
     * @param jsonDataInputStream json-data formated input stream in UTF-8
     * @param initalisedProperties instance of the properties to iniitalize with the json data.
     * @return the same instance as initalisedProperties setup with the json-data values.
     */
    public <P extends Properties> P toProperties(InputStream jsonDataStream, P initalisedProperties) {
        return JsonSchemaUtil.fromJson(jsonDataStream, initalisedProperties);
    }

    /**
     * Creates a ui-spec representation of the properties including json-schema, json-ui and json-data
     * 
     * @param properties instance of the properties to serialize.
     * @return json string in ui-specs representation of the data.
     */
    public String toJson(Properties properties) {
        return JsonSchemaUtil.toJson(properties);
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
