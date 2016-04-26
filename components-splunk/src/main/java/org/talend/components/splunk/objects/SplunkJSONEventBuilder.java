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


/**
 * created by dmytro.chmyga on Apr 26, 2016
 */
public class SplunkJSONEventBuilder {

    public static SplunkJSONEvent createEvent() {
        return new SplunkJSONEvent();
    }
    
    public static SplunkJSONEvent setField(SplunkJSONEvent event, String fieldName, Object value, boolean possibleMetadataField) {
        if(fieldName == null || value == null) {
            return event;
        }
        if(possibleMetadataField && SplunkJSONEventField.isMetadataField(fieldName)) {
            event.put(fieldName, value);
        } else {
            event.addEventObject(fieldName, value);
        }
        return event;
    }
    
}
