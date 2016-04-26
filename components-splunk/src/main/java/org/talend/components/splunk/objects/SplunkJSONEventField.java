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
package org.talend.components.splunk.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * created by dmytro.chmyga on Apr 26, 2016
 */
public enum SplunkJSONEventField {

    EVENT("event", Object.class),
    TIME("time", Double.class),
    SOURCE("source", String.class),
    SOURCE_TYPE("sourcetype", String.class),
    HOST("host", String.class),
    INDEX("index", String.class);
    
    private final String name;
    private final Class<?> dataType;
    private static List<SplunkJSONEventField> metadataFields;
    
    private SplunkJSONEventField(String name, Class<?> dataType) {
        this.name = name;
        this.dataType = dataType;
    }
    
    public String getName() {
        return name;
    }
    
    public Class<?> getDataType() {
        return dataType;
    }
    
    public static boolean isMetadataField(String fieldName) {
        if(fieldName == null || fieldName.isEmpty() || fieldName.equals(EVENT.name)) {
            return false;
        }
        for(SplunkJSONEventField field : values()) {
            if(fieldName.equals(field.name)) {
                return true;
            }
        }
        return false;
    }
    
    public static SplunkJSONEventField getByName(String name) {
        if(name == null || name.trim().isEmpty()) {
            return null;
        }
        for(SplunkJSONEventField field : values()) {
            if(name.equals(field.name)) {
                return field;
            }
        }
        return null;
    }
    
    public static List<SplunkJSONEventField> getMetadataFields() {
        if(metadataFields == null) {
            metadataFields = new ArrayList<>();
            for(SplunkJSONEventField field : values()) {
                if(field != EVENT) {
                    metadataFields.add(field);
                }
            }
        }
        return new ArrayList<>(metadataFields);
    }
    
}
