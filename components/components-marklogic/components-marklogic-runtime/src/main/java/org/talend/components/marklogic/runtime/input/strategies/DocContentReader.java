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
package org.talend.components.marklogic.runtime.input.strategies;

import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.io.BytesHandle;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.talend.components.marklogic.exceptions.MarkLogicErrorCode;
import org.talend.components.marklogic.exceptions.MarkLogicException;

import java.io.File;

public class DocContentReader {
    private DocumentManager docManager;

    private Class<?> docContentType;

    private Schema schema;

    public DocContentReader(DocumentManager docManager, Schema schema, Schema.Field docContentField) {
        this.docManager = docManager;
        if (docContentField != null) {
            this.docContentType = readDocContentTypeFromSchema(docContentField);
        }
        this.schema = schema;
    }

    private Class<?> readDocContentTypeFromSchema(Schema.Field docContentField) {
        Schema.Type schemaType = docContentField.schema().getType();
        switch (schemaType) {
            case BYTES: {
                return byte[].class;
            }
            case STRING: {
                String talendType;
                if ((talendType = docContentField.getProp("di.column.talendType")) != null) {
                    if (talendType.equals("id_Document")) {
                        return java.io.File.class;
                    } else if (talendType.equals("id_Object")) {
                        return java.io.InputStream.class;
                    }
                }
            }
        }
        return String.class;
    }

    private Object readDocContent(String docId) {
        Object docContent = null;

        if (docContentType == byte[].class){
            docContent = ((BytesHandle)docManager.read(docId, new BytesHandle())).get();
        } else if (docContentType == File.class) {
            try {
                docContent = new SAXReader().read((File) docManager.readAs(docId, docContentType));
            }
            catch (DocumentException e) {
                throw new MarkLogicException(new MarkLogicErrorCode("Can't read document"), e);
            }

        } else {
            docContent = docManager.readAs(docId, docContentType);
        }

        return docContent;
    }

    public GenericData.Record readDocument(String docId) {
        GenericData.Record documentRecord = new GenericData.Record(schema);

        documentRecord.put(0, docId);
        if (schema.getFields().size() > 1) {
            documentRecord.put(1, readDocContent(docId));
        }

        return documentRecord;
    }
}
