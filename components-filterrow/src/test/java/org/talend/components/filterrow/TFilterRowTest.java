
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
package org.talend.components.filterrow;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;

@SuppressWarnings("nls")
public class TFilterRowTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentService componentService;

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test.
    public ComponentService getComponentService() {
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();
            testComponentRegistry.registerComponentFamilyDefinition(new TFilterRowFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void testTFilterRowRuntime() throws Exception {
        TFilterRowProperties props = (TFilterRowProperties) getComponentService().getComponentProperties("tFilterRow_POC");

        // Set up the test schema - not really used for anything now
        Schema schema = SchemaBuilder.builder().record("testRecord").fields().name("field1").type().stringType().noDefault()
                .endRecord();
        // props.schema.schema.setValue(schema);
        //
        // File temp = File.createTempFile("TFilterRowtestFile", ".txt");
        // try {
        // PrintWriter writer = new PrintWriter(temp.getAbsolutePath(), "UTF-8");
        // writer.println("The first line");
        // writer.println("The second line");
        // writer.close();
        //
        // props.filename.setValue(temp.getAbsolutePath());
        // Source source = new TFilterRowSource();
        // source.initialize(null, props);
        // assertThat(source, instanceOf(TFilterRowSource.class));
        //
        // Reader<?> reader = ((BoundedSource) source).createReader(null);
        // assertThat(reader.start(), is(true));
        // assertThat(reader.getCurrent(), is((Object) "The first line"));
        // // No auto advance when calling getCurrent more than once.
        // assertThat(reader.getCurrent(), is((Object) "The first line"));
        // assertThat(reader.advance(), is(true));
        // assertThat(reader.getCurrent(), is((Object) "The second line"));
        // assertThat(reader.advance(), is(false));
        // } finally {// remote the temp file
        // temp.delete();
        // }
    }

}
