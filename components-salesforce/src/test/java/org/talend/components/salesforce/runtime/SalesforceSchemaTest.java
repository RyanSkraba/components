package org.talend.components.salesforce.runtime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.test.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.components.salesforce.tsalesforceoutputbulkexec.TSalesforceOutputBulkExecDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulkexec.TSalesforceOutputBulkExecProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class SalesforceSchemaTest extends SalesforceTestBase {

    Schema schema = SchemaBuilder.builder().record("metadata").fields()
        .name("col_1").type().stringType().noDefault()
        .name("col_2").type().intType().noDefault()
        .name("col_3").prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'").type(AvroUtils._date()).noDefault()
        .endRecord();

    @Test
    public void testOutputSchemaForTSalesforceBulkExec() throws Throwable {
        TSalesforceBulkExecDefinition defin = (TSalesforceBulkExecDefinition) getComponentService()
                .getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        TSalesforceBulkExecProperties modelProps = (TSalesforceBulkExecProperties) defin.createProperties();
        
        assertBulk(modelProps);
    }
    
    @Test
    public void testOutputSchemaForTSalesforceOutput1() throws Throwable {
        TSalesforceOutputDefinition defin = (TSalesforceOutputDefinition) getComponentService()
                .getComponentDefinition(TSalesforceOutputDefinition.COMPONENT_NAME);
        TSalesforceOutputProperties modelProps = (TSalesforceOutputProperties) defin.createProperties();
        
        modelProps.module.main.schema.setValue(schema);
        
        modelProps.extendInsert.setValue(false);
        modelProps.retrieveInsertId.setValue(true);
        modelProps.outputAction.setValue(OutputAction.INSERT);
        
        getComponentService().afterProperty("schema", modelProps.module.main);
        
        Schema output = modelProps.schemaFlow.schema.getValue();
        
        assertThat(output.getType(), is(Schema.Type.RECORD));
        assertThat(output.getFields(), hasSize(4));
        
        assertThat(output.getFields().get(0).name(), is("col_1"));
        assertThat(output.getFields().get(0).schema().getType(), is(Schema.Type.STRING));
        
        assertThat(output.getFields().get(1).name(), is("col_2"));
        assertThat(output.getFields().get(1).schema().getType(), is(Schema.Type.INT));
        
        assertThat(output.getFields().get(2).name(), is("col_3"));
        assertThat(output.getFields().get(2).schema().getType(), is(Schema.Type.LONG));
        assertThat(output.getFields().get(2).getProp(SchemaConstants.TALEND_COLUMN_PATTERN), is("yyyy-MM-dd'T'HH:mm:ss'.000Z'"));
        
        assertThat(output.getFields().get(3).name(), is("salesforce_id"));
        assertThat(output.getFields().get(3).schema().getType(), is(Schema.Type.STRING));
        
        Schema reject = modelProps.schemaReject.schema.getValue();
        
        assertThat(reject.getType(), is(Schema.Type.RECORD));
        assertThat(reject.getFields(), hasSize(6));
        
        assertThat(reject.getFields().get(0).name(), is("col_1"));
        assertThat(reject.getFields().get(0).schema().getType(), is(Schema.Type.STRING));
        
        assertThat(reject.getFields().get(1).name(), is("col_2"));
        assertThat(reject.getFields().get(1).schema().getType(), is(Schema.Type.INT));
        
        assertThat(reject.getFields().get(2).name(), is("col_3"));
        assertThat(reject.getFields().get(2).schema().getType(), is(Schema.Type.LONG));
        assertThat(reject.getFields().get(2).getProp(SchemaConstants.TALEND_COLUMN_PATTERN), is("yyyy-MM-dd'T'HH:mm:ss'.000Z'"));
        
        assertThat(reject.getFields().get(3).name(), is("errorCode"));
        assertThat(reject.getFields().get(3).schema().getType(), is(Schema.Type.STRING));
        
        assertThat(reject.getFields().get(4).name(), is("errorFields"));
        assertThat(reject.getFields().get(4).schema().getType(), is(Schema.Type.STRING));
        
        assertThat(reject.getFields().get(5).name(), is("errorMessage"));
        assertThat(reject.getFields().get(5).schema().getType(), is(Schema.Type.STRING));
    }
    
    @Test
    public void testOutputSchemaForTSalesforceOutputBulkExec() throws Throwable {
        TSalesforceOutputBulkExecDefinition defin = (TSalesforceOutputBulkExecDefinition) getComponentService()
                .getComponentDefinition(TSalesforceOutputBulkExecDefinition.COMPONENT_NAME);
        TSalesforceOutputBulkExecProperties modelProps = (TSalesforceOutputBulkExecProperties) defin.createProperties();
        
        assertBulk(modelProps);
    }

    private void assertBulk(TSalesforceBulkExecProperties modelProps) throws Throwable {
        modelProps.module.main.schema.setValue(schema);
        
        getComponentService().afterProperty("schema", modelProps.module.main);
        
        Schema output = modelProps.schemaFlow.schema.getValue();
        
        assertThat(output.getType(), is(Schema.Type.RECORD));
        assertThat(output.getFields(), hasSize(5));
        
        assertThat(output.getFields().get(0).name(), is("col_1"));
        assertThat(output.getFields().get(0).schema().getType(), is(Schema.Type.STRING));
        
        assertThat(output.getFields().get(1).name(), is("col_2"));
        assertThat(output.getFields().get(1).schema().getType(), is(Schema.Type.INT));
        
        assertThat(output.getFields().get(2).name(), is("col_3"));
        assertThat(output.getFields().get(2).schema().getType(), is(Schema.Type.LONG));
        assertThat(output.getFields().get(2).getProp(SchemaConstants.TALEND_COLUMN_PATTERN), is("yyyy-MM-dd'T'HH:mm:ss'.000Z'"));
        
        assertThat(output.getFields().get(3).name(), is("salesforce_id"));
        assertThat(output.getFields().get(3).schema().getType(), is(Schema.Type.STRING));
        
        assertThat(output.getFields().get(4).name(), is("salesforce_created"));
        assertThat(output.getFields().get(4).schema().getType(), is(Schema.Type.STRING));
        
        Schema reject = modelProps.schemaReject.schema.getValue();
        
        assertThat(reject.getType(), is(Schema.Type.RECORD));
        assertThat(reject.getFields(), hasSize(4));
        
        assertThat(reject.getFields().get(0).name(), is("col_1"));
        assertThat(reject.getFields().get(0).schema().getType(), is(Schema.Type.STRING));
        
        assertThat(reject.getFields().get(1).name(), is("col_2"));
        assertThat(reject.getFields().get(1).schema().getType(), is(Schema.Type.INT));
        
        assertThat(reject.getFields().get(2).name(), is("col_3"));
        assertThat(reject.getFields().get(2).schema().getType(), is(Schema.Type.LONG));
        assertThat(reject.getFields().get(2).getProp(SchemaConstants.TALEND_COLUMN_PATTERN), is("yyyy-MM-dd'T'HH:mm:ss'.000Z'"));
        
        assertThat(reject.getFields().get(3).name(), is("error"));
        assertThat(reject.getFields().get(3).schema().getType(), is(Schema.Type.STRING));
    }

}
