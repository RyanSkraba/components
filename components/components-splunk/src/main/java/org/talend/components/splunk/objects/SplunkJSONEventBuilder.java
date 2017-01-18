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
package org.talend.components.splunk.objects;

import java.util.Date;
import java.util.Locale;

public class SplunkJSONEventBuilder {

    public static SplunkJSONEvent createEvent() {
        return new SplunkJSONEvent();
    }

    public static SplunkJSONEvent setField(SplunkJSONEvent event, String fieldName, Object value, boolean possibleMetadataField) {
        if (fieldName == null || value == null) {
            return event;
        }
        if (possibleMetadataField && SplunkJSONEventField.isMetadataField(fieldName)) {
            event.put(fieldName, convertData(fieldName, value));
        } else {
            event.addEventObject(fieldName, value);
        }
        return event;
    }

    private static Object convertData(String fieldName, Object value) {
        if (SplunkJSONEventField.TIME.getName().equals(fieldName) && value instanceof Date) {
            double newValue = ((Date) value).getTime() / 1000.0;
            return String.format(Locale.ROOT, "%.3f", newValue);
        }
        return value;
    }

}
