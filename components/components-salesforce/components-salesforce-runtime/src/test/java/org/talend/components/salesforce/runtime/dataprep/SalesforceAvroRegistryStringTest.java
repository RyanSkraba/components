package org.talend.components.salesforce.runtime.dataprep;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.salesforce.runtime.SalesforceSchemaConstants;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

public class SalesforceAvroRegistryStringTest {

    @Test
    public void testPickList() throws Exception {
        DescribeSObjectResult describeSObjectResult = new DescribeSObjectResult();
        Field pickList = new Field();
        pickList.setName("pickList");
        pickList.setType(FieldType.picklist);
        describeSObjectResult.setFields(new Field[] { pickList });

        Schema schema = SalesforceAvroRegistryString.get().inferSchema(describeSObjectResult);

        assertThat(1, is(schema.getFields().size()));
    }

    @Test
    public void testPickListWithParent() throws Exception {
        DescribeSObjectResult describeSObjectResult = new DescribeSObjectResult();
        Field pickList = new Field();
        pickList.setName("pickList");
        pickList.setType(FieldType.picklist);
        pickList.setCompoundFieldName("parent");
        describeSObjectResult.setFields(new Field[] { pickList });

        Schema schema = SalesforceAvroRegistryString.get().inferSchema(describeSObjectResult);

        assertTrue(schema.getFields().isEmpty());
    }

    @Test
    public void testAddressTypeFilter() throws Exception {
        DescribeSObjectResult describeSObjectResult = new DescribeSObjectResult();
        Field address = new Field();
        address.setName("address");
        address.setType(FieldType.address);
        describeSObjectResult.setFields(new Field[] { address });

        Schema schema = SalesforceAvroRegistryString.get().inferSchema(describeSObjectResult);

        assertThat(0, is(schema.getFields().size()));
    }

    @Test
    public void testLocationTypeFilter() throws Exception {
        DescribeSObjectResult describeSObjectResult = new DescribeSObjectResult();
        Field location = new Field();
        location.setName("location");
        location.setType(FieldType.location);
        describeSObjectResult.setFields(new Field[] { location });

        Schema schema = SalesforceAvroRegistryString.get().inferSchema(describeSObjectResult);

        assertThat(0, is(schema.getFields().size()));
    }

    @Test
    public void testInferSchemaWithReferenceField() {
        Field referenceField = new Field();
        referenceField.setName("reference");
        referenceField.setType(FieldType.string);
        referenceField.setReferenceTo(new String[]{"SomeRecord"});
        referenceField.setRelationshipName("relationship");

        DescribeSObjectResult dsor = new DescribeSObjectResult();
        dsor.setName("MySObjectRecord");
        dsor.setFields(new Field[] { referenceField });

        Schema schema = SalesforceAvroRegistryString.get().inferSchema(dsor);

        Schema.Field field = schema.getField("reference");

        assertThat(field.schema().getType(), is(Schema.Type.STRING));
        assertThat(field.getProp(SalesforceSchemaConstants.REF_MODULE_NAME), is("SomeRecord"));
        assertThat(field.getProp(SalesforceSchemaConstants.REF_FIELD_NAME), is("relationship"));
    }

}