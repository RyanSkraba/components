// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jira.datum;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Utility class for retrieving certain values from Entity JSON representation
 */
abstract class EntityParser {

    private static final Logger LOG = LoggerFactory.getLogger(EntityParser.class);

    /**
     * Constant value, which means that positive integer value was not specified
     */
    static final int UNDEFINED = -1;

    /**
     * Constant value, which is with by braceCounter, to specify end of JSON
     */
    private static final int END_JSON = 0;

    /**
     * Parses JSON and searches for total property
     * 
     * @param json JSON string
     * @return total property value, if it is exist or -1 otherwise
     */
    static int getTotal(String json) {
        JsonFactory factory = new JsonFactory();
        try {
            JsonParser parser = factory.createParser(json);

            boolean totalFound = rewindToField(parser, "total");

            if (!totalFound) {
                return UNDEFINED;
            }

            // get total value
            String value = parser.getText();

            return Integer.parseInt(value);
        } catch (IOException e) {
            LOG.debug("Exception during JSON parsing. {}", e.getMessage());
        }
        return UNDEFINED;
    }

    /**
     * Parses JSON and returns a {@link List} of {@link Entity}
     * 
     * @param json JSON string
     * @param fieldName Name of field, from which retrieve a list of {@link Entity}
     * @return a {@link List} of {@link Entity}
     */
    static List<Entity> getEntities(String json, final String fieldName) {

        if (fieldName != null) {
            // cuts input JSON to parse only entities
            json = json.substring(json.indexOf(fieldName));
            // cuts input JSON to array of entities
            json = json.substring(json.indexOf('['));
            json = json.substring(0, json.lastIndexOf(']') + 1);
        }

        List<Entity> entities = new LinkedList<Entity>();
        StringBuilder entityBuilder = null;
        State currentState = State.INITIAL;
        /*
         * This counter counts braces '{' and '}'. It is used to define
         * start and end of JSON objects
         */
        int openedBraces = 0;
        char prev = ' ';

        for (char cur : json.toCharArray()) {

            switch (currentState) {
            case INITIAL: {
                if (cur == '[') {
                    currentState = State.READ_JSON_ARRAY;
                }
                break;
            }
            case READ_JSON_ARRAY: {
                if (cur == '{') {
                    currentState = State.READ_JSON_OBJECT;
                    entityBuilder = new StringBuilder();
                    entityBuilder.append(cur);
                    openedBraces++;
                }
                if (cur == ']') {
                    currentState = State.INITIAL;
                }
                break;
            }
            case READ_JSON_OBJECT: {
                entityBuilder.append(cur);
                if (cur == '{') {
                    openedBraces++;
                }
                if (cur == '}') {
                    openedBraces--;
                    if (openedBraces == 0) {
                        currentState = State.READ_JSON_ARRAY;
                        Entity entity = new Entity(entityBuilder.toString());
                        entities.add(entity);
                    }
                }
                if (cur == '"') {
                    currentState = State.READ_JSON_STRING;
                }
                break;
            }
            case READ_JSON_STRING: {
                entityBuilder.append(cur);
                if (cur == '"' && prev == '\\') {
                    int countBackslash = 0;
                    for (int k = entityBuilder.length() - 2; k > 0; k--) {
                        if (entityBuilder.charAt(k) != '\\' && countBackslash % 2 == 0) {
                            currentState = State.READ_JSON_OBJECT;
                            break;
                        }
                        countBackslash++;
                    }

                } else if (cur == '"' && prev != '\\') {
                    currentState = State.READ_JSON_OBJECT;
                }
                break;
            }
            }
            prev = cur;
        }
        return entities;
    }

    /**
     * Rewinds {@link JsonParser} to the value of specified field
     * 
     * @param parser JSON parser
     * @param fieldName name of field rewind to
     * @return true if field was found, false otherwise
     * @throws IOException in case of exception during JSON parsing
     */
    private static boolean rewindToField(JsonParser parser, final String fieldName) throws IOException {

        JsonToken currentToken = parser.nextToken();
        /*
         * There is no special token, which denotes end of file, in Jackson.
         * This counter is used to define the end of file.
         * The counter counts '{' and '}'. It is increased, when meets '{' and
         * decreased, when meets '}'. When braceCounter == 0 it means the end
         * of file was met
         */
        int braceCounter = 0;
        String currentField = null;
        do {
            if (JsonToken.START_OBJECT == currentToken) {
                braceCounter++;
            }
            if (JsonToken.END_OBJECT == currentToken) {
                braceCounter--;
            }
            if (JsonToken.FIELD_NAME == currentToken) {
                currentField = parser.getCurrentName();
            }
            currentToken = parser.nextToken();
        } while (!fieldName.equals(currentField) && braceCounter != END_JSON);

        return braceCounter != END_JSON;
    }

    /**
     * Entity Parser state
     */
    private enum State {
        INITIAL,
        READ_JSON_OBJECT,
        READ_JSON_ARRAY,
        READ_JSON_STRING
    }

}
