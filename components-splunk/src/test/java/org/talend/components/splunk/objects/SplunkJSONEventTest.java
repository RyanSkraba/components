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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.junit.Test;

public class SplunkJSONEventTest {

    @Test
    public void testSplunkJSONEventFieldsSetting() {
        SplunkJSONEvent splunkEvent = new SplunkJSONEvent();

        // Test setting some metadata fields
        splunkEvent.put(SplunkJSONEventField.INDEX, "index");
        splunkEvent.put(SplunkJSONEventField.SOURCE, "localhost");

        // Assert set fields
        assertTrue("Index should be equal to index", "index".equals(splunkEvent.get(SplunkJSONEventField.INDEX.getName())));
        assertTrue("Source should be equal to localhost",
                "localhost".equals(splunkEvent.get(SplunkJSONEventField.SOURCE.getName())));

        // Try to set null values. It shouldn't be set.
        splunkEvent.put(SplunkJSONEventField.HOST, null);
        assertFalse("Host value was set to null. It shouldn't be set.",
                splunkEvent.containsKey(SplunkJSONEventField.HOST.getName()));

        // Try to add event field
        splunkEvent.addEventObject("Field1", "Value1");
        splunkEvent.addEventObject("FieldInt", 123);

        // Check event fields
        JSONObject eventObject = (JSONObject) splunkEvent.get(SplunkJSONEventField.EVENT.getName());
        assertFalse("Event Field should not be null", eventObject == null);

        assertTrue("Field1 value should be equal to Value1", "Value1".equals(eventObject.remove("Field1")));
        assertTrue("FieldInt value should be int value and be equal to 123", (int) eventObject.remove("FieldInt") == 123);

        // Nothing else should be in the event object
        assertTrue("Nothing should be left in the event object", eventObject.size() == 0);
    }

    @Test
    public void testSplunkJSONEventBuilder() {
        SplunkJSONEvent event = SplunkJSONEventBuilder.createEvent();
        SplunkJSONEventBuilder.setField(event, "source", "source", true);

        // Check that "source" metadata field was set
        assertTrue("Source metadata field should have been set", "source".equals(event.get("source")));

        // Set event field
        SplunkJSONEventBuilder.setField(event, "Field1", "Value1", true);

        JSONObject eventData = (JSONObject) event.get("event");
        // Check event data field
        assertTrue("Field1 should be equal to Value1", "Value1".equals(eventData.get("Field1")));
    }

}
