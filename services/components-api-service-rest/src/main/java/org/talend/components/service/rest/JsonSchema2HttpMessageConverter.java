package org.talend.components.service.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.jsonschema.JsonUtil;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class JsonSchema2HttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * Sets following mime type headers: application/json;charset=UTF-8 application/*+json;charset=UTF-8
     */
    public JsonSchema2HttpMessageConverter() {
        super(new MediaType("application", "json", DEFAULT_CHARSET), new MediaType("application", "*+json", DEFAULT_CHARSET));
    }

    /**
     * Serializes specified Object and writes JSON to HTTP message body
     *
     * @param t Object to serialize
     * @param outputMessage the HTTP output message to write to
     */
    @Override
    protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputMessage.getBody());
        String objectToJson = null;
        if (Properties.class.isAssignableFrom(t.getClass())) {
            objectToJson = JsonUtil.toJson((Properties) t, true);
        } else {
            objectToJson = JsonWriter.objectToJson(t);
        }

        outputStreamWriter.write(objectToJson);
        outputStreamWriter.flush();
        outputStreamWriter.close();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation doesn't and shouldn't support swagger object.
     */
    @Override
    protected boolean supports(Class<?> clazz) {
        // the following exception is used to exclude this json message converter to be used for swagger.
        // otherwise the generated json is no more compatibile with swagger standard.
        return !clazz.getName().startsWith("com.mangofactory");
    }

    /**
     * Deserializes JSON to object
     *
     * @param clazz type of object to return. Not used
     * @param inputMessage the HTTP input message to read from
     * @return deserialized object
     */
    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        if (Properties.class.isAssignableFrom(clazz)) {
            try {
                return JsonUtil.fromJson(inputMessage.getBody());
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        } else {
            return JsonReader.jsonToJava(inputMessage.getBody(), null);
        }
    }
}