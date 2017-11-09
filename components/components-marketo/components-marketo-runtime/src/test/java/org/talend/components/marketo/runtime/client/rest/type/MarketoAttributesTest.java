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
package org.talend.components.marketo.runtime.client.rest.type;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.Gson;

public class MarketoAttributesTest {

    private static final String act1 = "{\"id\":36356713,\"marketoGUID\":\"36356713\",\"leadId\":5278339,\"activityDate\":\"2017-11-07T07:36:03Z\",\"activityTypeId\":13,\"primaryAttributeValueId\":721,\"primaryAttributeValue\":\"Lead Currency\",\"attributes\":[{\"name\":\"New Value\",\"value\":\"USD\"},{\"name\":\"Old Value\",\"value\":null},{\"name\":\"Reason\",\"value\":\"Synched from salesforce.com Lead ID 00Q2F0000025kWvUAI\"},{\"name\":\"Source\",\"value\":\"sfdc\"}]}";

    private static final String act2 = "{\"id\":36356713,\"marketoGUID\":\"36356713\",\"leadId\":5278339,"
            + "\"activityDate\":\"2017-11-07T07:36:03Z\",\"activityTypeId\":13,\"primaryAttributeValueId\":721,"
            + "\"primaryAttributeValue\":\"Lead Currency\",\"attributes\":[{\"name\":\"Data Value Changes\",\"value\":[{\"New Value\":\"2017-45\",\"Attribute Name\":\"Business Week\",\"Old Value\":\"2017-44\"}]}]}";

    private Gson gson = new Gson();

    @Test
    public void testActivitySimpleAttributes() throws Exception {
        LeadActivityRecord activity = gson.fromJson(act1, LeadActivityRecord.class);
        assertEquals(Integer.valueOf(36356713), activity.getId());
        assertEquals(Integer.valueOf(5278339), activity.getLeadId());
        assertEquals(Integer.valueOf(13), activity.getActivityTypeId());
        assertEquals("36356713", activity.getMarketoGUID());
        assertEquals("null", activity.getMktoAttributes().get("Old_Value"));
        assertEquals("USD", activity.getMktoAttributes().get("New_Value"));
        assertEquals("Synched from salesforce.com Lead ID 00Q2F0000025kWvUAI", activity.getMktoAttributes().get("Reason"));
        assertEquals("sfdc", activity.getMktoAttributes().get("Source"));
    }

    @Test
    public void testActivityNestedAttributes() throws Exception {
        LeadActivityRecord activity = gson.fromJson(act2, LeadActivityRecord.class);
        assertEquals(Integer.valueOf(36356713), activity.getId());
        assertEquals(Integer.valueOf(5278339), activity.getLeadId());
        assertEquals(Integer.valueOf(13), activity.getActivityTypeId());
        assertEquals("36356713", activity.getMarketoGUID());
        assertEquals("[{New Value=2017-45, Attribute Name=Business Week, Old Value=2017-44}]",
                activity.getMktoAttributes().get("Data_Value_Changes"));
    }

}
