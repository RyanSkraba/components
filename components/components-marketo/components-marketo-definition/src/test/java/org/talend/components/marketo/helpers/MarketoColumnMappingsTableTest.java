// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.helpers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.avro.SchemaConstants;

public class MarketoColumnMappingsTableTest {

    MarketoColumnMappingsTable mappings;

    @Before
    public void setup() {
        mappings = new MarketoColumnMappingsTable("test");
        mappings.setupProperties();
    }

    @Test
    public void testGetNameMappingsForMarketoEmpty() throws Exception {
        assertEquals(Collections.emptyMap(), mappings.getNameMappingsForMarketo());
        List<String> schema = new ArrayList<String>();
        List<String> mkto = new ArrayList<String>();
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);
        assertEquals(Collections.emptyMap(), mappings.getNameMappingsForMarketo());
    }

    @Test
    public void testGetNameMappingsForMarketoNoMktoMappings() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("", "", ""));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);

        assertEquals(schema, Arrays.asList(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(schema, Arrays.asList(mappings.getNameMappingsForMarketo().values().toArray()));
    }

    @Test
    public void testGetNameMappingsForMarketoFullMappings() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("LeadId", "Email", "Campaign"));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);

        assertEquals(schema, Arrays.asList(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(mkto, Arrays.asList(mappings.getNameMappingsForMarketo().values().toArray()));
    }

    @Test
    public void testGetNameMappingsForMarketoSemiMappings() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("LeadId", "", "Campaign"));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);

        assertEquals(schema, Arrays.asList(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(Arrays.asList("LeadId", "EMAIL", "Campaign"),
                Arrays.asList(mappings.getNameMappingsForMarketo().values().toArray()));
    }

    @Test
    public void testGetNameMappingsForMarketoSemiMappingsWithNull() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("LeadId", null, ""));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);
        assertEquals(schema, Arrays.asList(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(Arrays.asList("LeadId", "EMAIL", "CAMPAIGN"),
                Arrays.asList(mappings.getNameMappingsForMarketo().values().toArray()));
    }

    public Schema getTestSchema() {
        return SchemaBuilder.record("test").fields()//
                .name("ID").type().stringType().noDefault()//
                .name("EMAIL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "EMAIL")//
                .type().stringType().noDefault()//
                .name("CAMPAIGN").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "Campaign")//
                .type().stringType().noDefault()//
                .name("LastScoring_Adoption").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "LastScoring-Adoption")//
                .type().stringType().noDefault()//
                //
                .endRecord();
    }

    @Test
    public void testGetMarketoColumnsFullMappings() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN", "LastScoring_Adoption"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("LeadId", "Email", "Campaign", "LastScoring-Adoption"));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);

        assertThat(schema, containsInAnyOrder(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(mkto, mappings.getMarketoColumns(getTestSchema()));
    }

    @Test
    public void testGetMarketoColumnsSemiMappings() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN", "LastScoring_Adoption"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("LeadId", "", "Campaign", ""));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);

        assertThat(schema, containsInAnyOrder(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(Arrays.asList("LeadId", "EMAIL", "Campaign", "LastScoring-Adoption"),
                mappings.getMarketoColumns(getTestSchema()));
    }

    @Test
    public void testGetMarketoColumnsSemiMappingsWithNull() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN", "LastScoring_Adoption"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("LeadId", null, "", ""));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);

        assertThat(schema, containsInAnyOrder(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(Arrays.asList("LeadId", "EMAIL", "Campaign", "LastScoring-Adoption"),
                mappings.getMarketoColumns(getTestSchema()));
    }

    @Test
    public void testGetMarketoColumnsNoMappingsWithNull() throws Exception {
        List<String> schema = new ArrayList<String>(Arrays.asList("ID", "EMAIL", "CAMPAIGN", "LastScoring_Adoption"));
        List<String> mkto = new ArrayList<String>(Arrays.asList("", null, "", ""));
        mappings.columnName.setValue(schema);
        mappings.marketoColumnName.setValue(mkto);

        assertThat(schema, containsInAnyOrder(mappings.getNameMappingsForMarketo().keySet().toArray()));
        assertEquals(Arrays.asList("ID", "EMAIL", "Campaign", "LastScoring-Adoption"),
                mappings.getMarketoColumns(getTestSchema()));
    }

    @Test
    public void testGetInputedNameMappingsForMarketoNullAndEmptyValue() throws Exception {
        mappings.columnName.setValue(new ArrayList<String>());
        mappings.marketoColumnName.setValue(new ArrayList<String>());
        assertEquals(new HashMap<String, String>(), mappings.getInputedNameMappingsForMarketo());
        mappings.columnName.setValue(null);
        mappings.marketoColumnName.setValue(null);
        assertEquals(new HashMap<String, String>(), mappings.getInputedNameMappingsForMarketo());
    }

}
