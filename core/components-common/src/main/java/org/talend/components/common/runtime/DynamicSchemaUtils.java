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
package org.talend.components.common.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.exception.ExceptionContext.ExceptionContextBuilder;
import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This util's class is responsible for runtime operations performed on dynamic schema's.
 *
 */
public class DynamicSchemaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSchemaUtils.class);

    private static final I18nMessages I18N_MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(DynamicSchemaUtils.class);

    // Util's class shouldn't have public constructor.
    private DynamicSchemaUtils() {
    };

    /**
     * Compare remote schema and input schema with case insensitive condition for field names.
     * This method performs the same action on schema fields as LEFT OUTER JOIN in SQL.<br/>
     * Examples with possible cases:<br/>
     * <ol>
     * <li>When <b>remote schema</b> is <b>null</b> or <b>empty</b> - throw {@link ComponentException} with detailed message: you should specify your remote schema correctly.</li>
     * <li>When <b>input schema</b> is <b>null</b> - throw {@link ComponentException} with detailed message: you should specify your input schema correctly.</li>
     * <li>When <b>schemas</b> are <b>equal</b> - return all fields.</li>
     * <li>When <b>remote schema</b> has <b>more</b> fields than <b>input schema</b> - copy and add all common fields and fill with nulls other cells in result list to conform remote schema fields size</li>
     * <li>When <b>remote schema</b> has <b>less</b> fields than <b>input schema</b> - copy and add all common fields in result list</li>
     * </ol>
     *
     * @param remoteSchema - runtime schema got from data source.
     * @param inputSchema - schema, that is used in incoming {@link IndexedRecord} data.
     * @return all fields from the remote schema and the matched fields from the input schema.
     */
    public static List<Field> getCommonFieldsForDynamicSchema(Schema remoteSchema, Schema inputSchema) {
        if (remoteSchema == null || remoteSchema.getFields().isEmpty()) {
            throwSchemaValidationException("error.message.remoteSchemaNotSet");
        }

        if (inputSchema == null || inputSchema.getFields().isEmpty()) {
            throwSchemaValidationException("error.message.inputSchemaNotSet");
        }

        List<Schema.Field> commonFields = new ArrayList<>(remoteSchema.getFields().size());
        List<Schema.Field> inputFields = new ArrayList<>(inputSchema.getFields());
        boolean completelyDifferent = true;
        for (Schema.Field snowflakeRuntimeField : remoteSchema.getFields()) {
            boolean added = false;
            Iterator<Schema.Field> iterator = inputFields.iterator();
            while (iterator.hasNext()) {
                Schema.Field incomingField = iterator.next();
                if (incomingField.name().equalsIgnoreCase(snowflakeRuntimeField.name())) {
                    commonFields.add(incomingField);
                    // We need to warn user about left unused columns. So delete used ones.
                    iterator.remove();
                    added = true;
                    completelyDifferent = false;
                    break;
                }
            }
            if (!added) {
                // Returned list of fields must have the same length as remote schema fields size.
                commonFields.add(null);
            }
        }

        if (completelyDifferent) {
            throwSchemaValidationException("error.message.differentSchema");
        }

        if (inputFields.size() != 0) {
            String[] names = new String[inputFields.size()];
            for (int i = 0; i < inputFields.size(); i++) {
                names[i] = inputFields.get(i).name();
            }
            LOGGER.warn(I18N_MESSAGES.getMessage("warning.message.unusedColumns", Arrays.toString(names)));
        }

        return commonFields;
    }

    /**
     * Creates and throws runtime exception with specified error message. Message gets from properties file.
     *
     * @param messagePropertyName key for specific error message.
     */
    private static void throwSchemaValidationException(String messagePropertyName) {
        throw new ComponentException(new DefaultErrorCode(HttpServletResponse.SC_BAD_REQUEST, "errorMessage"),
                new ExceptionContextBuilder().put("errorMessage", I18N_MESSAGES.getMessage(messagePropertyName)).build());
    }
}
