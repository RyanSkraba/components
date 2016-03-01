// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.container;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.daikon.schema.SchemaElement;

/**
 * An implementation of a runtime container for testing purposes.
 */
public class DefaultComponentRuntimeContainerImpl implements RuntimeContainer {

    private Map<String, Object> globalMap = new HashMap<>();

    private String currentComponentName;

    class Dynamic implements ComponentDynamicHolder {

        private List<SchemaElement> schemaElements;

        private Map<String, Object> values = new HashMap<>();

        @Override
        public List<SchemaElement> getSchemaElements() {
            return schemaElements;
        }

        @Override
        public void setSchemaElements(List<SchemaElement> elements) {
            this.schemaElements = elements;
        }

        @Override
        public Object getFieldValue(String fieldName) {
            return values.get(fieldName);
        }

        @Override
        public void addFieldValue(String fieldName, Object value) {
            values.put(fieldName, value);
        }

        @Override
        public void resetValues() {
            values.clear();
        }
    }

    @Override
    public String formatDate(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    @Override
    public Dynamic createDynamicHolder() {
        return new Dynamic();
    }

}
