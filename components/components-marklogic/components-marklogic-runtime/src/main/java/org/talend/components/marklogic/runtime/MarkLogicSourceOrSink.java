// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.runtime;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.marklogic.MarkLogicProvideConnectionProperties;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.exceptions.MarkLogicErrorCode;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

public class MarkLogicSourceOrSink extends MarkLogicConnection implements SourceOrSink {

    protected static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarkLogicSourceOrSink.class);

    protected MarkLogicProvideConnectionProperties ioProperties;
    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResultMutable vr = new ValidationResultMutable();
        try {
            connect(container);
        } catch (MarkLogicException me) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(me.getMessage());
        }
        return vr;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable();
        if (properties instanceof MarkLogicProvideConnectionProperties) {
            this.ioProperties = (MarkLogicProvideConnectionProperties) properties;
        }
        else {
            vr.setStatus(ValidationResult.Result.ERROR);
            vr.setMessage(MESSAGES.getMessage("error.wrongProperties"));
        }
        return vr;
    }

    @Override
    protected MarkLogicConnectionProperties getMarkLogicConnectionProperties() {
        return ioProperties.getConnectionProperties();
    }

    protected void checkDocContentTypeSupported(SchemaProperties outputSchema) {
        boolean isDocContentFieldPresent = outputSchema.schema.getValue().getFields().size() >= 2;
        if (isDocContentFieldPresent) {
            Schema.Field docContentField = outputSchema.schema.getValue().getFields().get(1);
            boolean isAvroStringOrBytes = ((docContentField.schema().getType().equals(Schema.Type.STRING)
                    || docContentField.schema().getType().equals(Schema.Type.BYTES))
                    && docContentField.schema().getProp(SchemaConstants.JAVA_CLASS_FLAG) == null);
            boolean isTalendTypeSupported = false;
            String talendType = docContentField.getProp("di.column.talendType");
            //component supports only Object, String, Document and byte[] types as docContent
            //Object and Document are present only in studio
            //so when talendType != null - it means that docContent type was changed in studio

            if (talendType != null) {
                isTalendTypeSupported = (talendType.equals("id_Object")
                        || talendType.equals("id_String")
                        || talendType.equals("id_Document")
                        || talendType.equals("id_byte[]"));
            }

            if (!(isAvroStringOrBytes || isTalendTypeSupported)) {
                throw new MarkLogicException(new MarkLogicErrorCode("Wrong docContent type"));
            }
        } else if (outputSchema.schema.getValue().getFields().size() != 1) { // it is ok to have only docId field
            throw new MarkLogicException(new MarkLogicErrorCode("No docContent field in the schema"));
        }
    }
}
