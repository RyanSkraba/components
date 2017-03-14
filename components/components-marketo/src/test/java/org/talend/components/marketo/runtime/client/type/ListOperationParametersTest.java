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
package org.talend.components.marketo.runtime.client.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ListOperationParametersTest {

    ListOperationParameters listOpREST;

    ListOperationParameters listOpSOAP;

    final String[] stringKeys = {"KEY1", "KEY2"};

    final Integer[] integerKeys = {1, 2};

    Integer listId = 1000;

    Integer leadId = 2000;

    @Before
    public void setUp() throws Exception {
        listOpREST = new ListOperationParameters();
        listOpREST.setApiMode("REST");
        listOpREST.setOperation("REMOVEFROM");
        listOpREST.setStrict(false);
        listOpREST.setListId(listId);
        listOpREST.setLeadIds(integerKeys);
        //
        listOpSOAP = new ListOperationParameters();
        listOpSOAP.setApiMode("SOAP");
        listOpSOAP.setOperation("MEMBEROF");
        listOpSOAP.setStrict(true);
        listOpSOAP.setListKeyValue(listId.toString());
        listOpSOAP.setLeadKeyValue(stringKeys);
    }

    @Test
    public void testSOAP() throws Exception {
        assertEquals("SOAP", listOpSOAP.getApiMode());
        assertEquals("MEMBEROF", listOpSOAP.getOperation());
        assertEquals("MKTOLISTNAME", listOpSOAP.getListKeyType());
        assertEquals(listId.toString(), listOpSOAP.getListKeyValue());
        assertEquals("IDNUM", listOpSOAP.getLeadKeyType());
        assertEquals(stringKeys, listOpSOAP.getLeadKeyValues());
        assertTrue(listOpSOAP.getStrict());
        assertEquals(2, listOpSOAP.getLeadKeyValue().size());
    }

    @Test
    public void testREST() throws Exception {
        assertEquals("REST", listOpREST.getApiMode());
        assertEquals("REMOVEFROM", listOpREST.getOperation());
        assertEquals("MKTOLISTNAME", listOpREST.getListKeyType());
        assertEquals("IDNUM", listOpREST.getLeadKeyType());
        assertEquals(listId, listOpREST.getListId());
        assertEquals(integerKeys, listOpREST.getLeadIdsValues());
        assertEquals(2, listOpREST.getLeadIds().size());
        assertFalse(listOpREST.getStrict());
        assertEquals(2, listOpREST.getLeadIds().size());

    }

    @Test
    public void testIsValid() throws Exception {
        assertTrue(listOpSOAP.isValid());
        listOpSOAP.setListKeyType(null);
        assertFalse(listOpSOAP.isValid());
        listOpSOAP.setListKeyType("");
        assertFalse(listOpSOAP.isValid());
        listOpSOAP.setListKeyValue(null);
        assertFalse(listOpSOAP.isValid());
        listOpSOAP.setListKeyValue("");
        assertFalse(listOpSOAP.isValid());

        listOpSOAP.setLeadKeyType(null);
        assertFalse(listOpSOAP.isValid());
        listOpSOAP.setLeadKeyType("");
        listOpSOAP.setLeadKeyValue(stringKeys);
        assertFalse(listOpSOAP.isValid());

        listOpSOAP.setLeadKeyType("FRRT");
        listOpSOAP.setLeadKeyValue(null);
        assertFalse(listOpSOAP.isValid());
        listOpSOAP.setLeadKeyValue(new String[]{});
        assertFalse(listOpSOAP.isValid());
        //
        assertTrue(listOpREST.isValid());
        listOpREST.setListId(null);
        listOpREST.setLeadIds(null);
        assertFalse(listOpREST.isValid());
        listOpREST.setLeadIds(new Integer[]{});
        assertFalse(listOpREST.isValid());
        listOpREST.setLeadIds(integerKeys);
        assertFalse(listOpREST.isValid());
        listOpREST.setListId(listId);
        listOpREST.setLeadIds(null);
        assertFalse(listOpREST.isValid());
        listOpREST.setListId(listId);
        listOpREST.setLeadIds(new Integer[]{});
        assertFalse(listOpREST.isValid());
        //
        ListOperationParameters p = new ListOperationParameters();
        assertFalse(p.isValid());
        p.setListId(listId);
        p.setLeadIds(integerKeys);
        assertFalse(p.isValid());
        p.setApiMode("SOAP");
        assertFalse(p.isValid());
        p.setApiMode("REST");
        assertTrue(p.isValid());
    }

    @Test
    public void testToString() throws Exception {
        String soap = "ListOperationParameters{operation='MEMBEROF', strict=true, isValid=true, apiMode='SOAP', "
                + "listKeyType='MKTOLISTNAME', listKeyValue='1000', leadKeyType='IDNUM', leadKeyValue=[KEY1, KEY2], "
                + "listId=0, leadIds=[]}";
        String rest = "ListOperationParameters{operation='REMOVEFROM', strict=false, isValid=true, apiMode='REST', listKeyType='MKTOLISTNAME', listKeyValue='', leadKeyType='IDNUM', leadKeyValue=[], listId=1000, leadIds=[1, 2]}";
        String nulls = "ListOperationParameters{operation='MEMBEROF', strict=true, isValid=false, apiMode='SOAP', "
                + "listKeyType='TEST1',"
                + " listKeyValue='1000', leadKeyType='TEST2', leadKeyValue=null, listId=0, leadIds=null}";
        assertEquals(soap, listOpSOAP.toString());
        assertEquals(rest, listOpREST.toString());
        listOpSOAP.setLeadIds(null);
        listOpSOAP.setLeadKeyValue(null);
        listOpSOAP.setListKeyType("TEST1");
        listOpSOAP.setLeadKeyType("TEST2");
        assertEquals(nulls, listOpSOAP.toString());
    }

}