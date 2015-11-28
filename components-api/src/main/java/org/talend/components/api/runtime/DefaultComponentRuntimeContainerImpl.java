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
package org.talend.components.api.runtime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.components.api.schema.SchemaElement;

/**
 * An implementation of a runtime container for testing purposes.
 */
public class DefaultComponentRuntimeContainerImpl implements ComponentRuntimeContainer {

    private Map<String, Object> globalMap = new HashMap<>();

    public Map<String, Object> getGlobalMap() {
        return globalMap;
    }

    class Dynamic implements ComponentDynamicHolder {

        List<SchemaElement> schemaElements;

        Map<String, Object> values;

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
            if (values == null)
                values = new HashMap<>();
            values.put(fieldName, value);
        }

        @Override
        public void resetValues() {
            values = null;
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
