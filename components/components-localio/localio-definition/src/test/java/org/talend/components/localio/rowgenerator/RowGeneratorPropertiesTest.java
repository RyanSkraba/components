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
package org.talend.components.localio.rowgenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.daikon.properties.Properties.Helper;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class RowGeneratorPropertiesTest {

    private Schema emptySchema = Schema.createRecord("EmptyRecord", null, null, false, new ArrayList<Schema.Field>());

    /**
     * Checks {@link RowGeneratorProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        RowGeneratorProperties properties = new RowGeneratorProperties("test");
        assertThat(properties.schemaFlow.schema.getValue(), nullValue());
        assertThat(properties.useSeed.getValue(), is(false));
        assertThat(properties.seed.getValue(), is(0L));
        assertThat(properties.nbRows.getValue(), is(100L));
        assertThat(properties.nbPartitions.getValue(), is(1));
        assertThat(properties.usePartitionSkew.getValue(), is(false));
        assertThat(properties.nbRowsMaxSkewed.getValue(), is(0L));

        properties.init();

        assertThat(properties.schemaFlow.schema.getValue(), equalTo(emptySchema));
    }

    @Test
    public void testPropertySerialization() {
        RowGeneratorProperties properties = new RowGeneratorProperties("test");
        properties.init();
        properties.useSeed.setValue(true);
        properties.seed.setValue(123L);
        properties.nbRows.setValue(200L);

        properties = Helper.fromSerializedPersistent(properties.toSerialized(), RowGeneratorProperties.class).object;

        assertThat(properties.schemaFlow.schema.getValue(), equalTo(emptySchema));
        assertThat(properties.useSeed.getValue(), equalTo(true));
        assertThat(properties.seed.getValue(), equalTo(123L));
        assertThat(properties.nbRows.getValue(), equalTo(200L));
    }

    /**
     * Checks {@link RowGeneratorProperties#setupLayout()} creates Main form.
     */
    @Test
    public void testSetupLayout() {
        RowGeneratorProperties properties = new RowGeneratorProperties("test");
        properties.init();
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(7));
        assertThat(main.getWidget("schemaFlow"), notNullValue());
        assertThat(main.getWidget("useSeed"), notNullValue());
        assertThat(main.getWidget("seed"), notNullValue());
        assertThat(main.getWidget("nbRows"), notNullValue());
        assertThat(main.getWidget("nbPartitions"), notNullValue());
        assertThat(main.getWidget("usePartitionSkew"), notNullValue());
        assertThat(main.getWidget("nbRowsMaxSkewed"), notNullValue());
    }

    /**
     * Checks {@link RowGeneratorProperties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayout() {
        RowGeneratorProperties properties = new RowGeneratorProperties("test");
        properties.init();
        properties.refreshLayout(properties.getForm(Form.MAIN));

        // The default layout.
        assertThat(properties.getForm(Form.MAIN).getWidget("schemaFlow").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("useSeed").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("seed").isVisible(), is(false));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbRows").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbPartitions").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("usePartitionSkew").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbRowsMaxSkewed").isVisible(), is(false));

        // Turn on useSeed
        properties.useSeed.setValue(true);
        properties.afterUseSeed();
        assertThat(properties.getForm(Form.MAIN).getWidget("schemaFlow").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("useSeed").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("seed").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbRows").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbPartitions").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("usePartitionSkew").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbRowsMaxSkewed").isVisible(), is(false));

        // Turn on usePartitionSkew
        properties.usePartitionSkew.setValue(true);
        properties.afterUsePartitionSkew();
        assertThat(properties.getForm(Form.MAIN).getWidget("schemaFlow").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("useSeed").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("seed").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbRows").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbPartitions").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("usePartitionSkew").isVisible(), is(true));
        assertThat(properties.getForm(Form.MAIN).getWidget("nbRowsMaxSkewed").isVisible(), is(true));

    }

    /**
     * Checks {@link RowGeneratorProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the
     * main link, for input link, for input
     */
    @Test
    public void testGetAllSchemaPropertiesConnectors() {
        RowGeneratorProperties properties = new RowGeneratorProperties("test");
        assertThat(properties.getAllSchemaPropertiesConnectors(false), hasSize(0));
        assertThat(properties.getAllSchemaPropertiesConnectors(true), contains(properties.FLOW_CONNECTOR));
    }
}
