package org.talend.components.salesforce.runtime;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.PartnerConnection;
import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.runtime.common.ConnectionHolder;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link SalesforceSourceOrSink} class
 */
public class SalesforceSourceOrSinkTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceSourceOrSinkTest.class);

    private static final String SPACE = " ";

    @Mock
    private RuntimeContainer runtimeContainerMock = Mockito.mock(RuntimeContainer.class);

    private SalesforceConnectionProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new SalesforceConnectionProperties("test");
    }

    /**
     * Checks {@link SalesforceSourceOrSink#guessSchema(String)} returns the
     * {@link org.apache.avro.Schema} with date and string type
     */
    @Test
    public void testGuessSchema() throws Exception {
        String field0Name = "Id";
        String field1Name = "LastModifiedDate";
        String field2Name = "LastActivityDate";

        String drivingEntity = "Account";

        String soql = new StringBuilder().append("SELECT").append(" ")
                .append(field0Name).append(",").append(SPACE)
                .append(field1Name).append(",").append(SPACE)
                .append(field2Name).append(SPACE)
                .append("FROM").append(SPACE)
                .append(drivingEntity).toString();

        final PartnerConnection partnerConnectionMock = Mockito.mock(PartnerConnection.class);

        class SalesforceSourceOrSinkChild extends SalesforceSourceOrSink {
            @Override
            protected ConnectionHolder connect(RuntimeContainer container) {
                ConnectionHolder connectionHolder = new ConnectionHolder();
                connectionHolder.connection = partnerConnectionMock;
                return connectionHolder;
            }
        }

        Field field0 = new Field();
        field0.setName(field0Name);
        field0.setType(FieldType.string);

        Field field1 = new Field();
        field1.setName(field1Name);
        field1.setType(FieldType.date);

        Field field2 = new Field();
        field2.setName(field2Name);
        field2.setType(FieldType.date);

        Field[] fields = new Field[3];
        fields[0] = field0;
        fields[1] = field1;
        fields[2] = field2;

        DescribeSObjectResult describeSObjectResult = new DescribeSObjectResult();
        describeSObjectResult.setFields(fields);

        Mockito.when(partnerConnectionMock.describeSObject(drivingEntity)).thenReturn(describeSObjectResult);

        SalesforceSourceOrSinkChild salesforceSourceOrSinkChild = new SalesforceSourceOrSinkChild();
        salesforceSourceOrSinkChild.initialize(runtimeContainerMock, properties);

        Schema resultSchema = salesforceSourceOrSinkChild.guessSchema(soql);

        LOGGER.debug("result schema: " + resultSchema.toString());

        Assert.assertEquals("GuessedSchema", resultSchema.getName());

        Assert.assertEquals(field0Name, resultSchema.getFields().get(0).name());


        Assert.assertEquals(field1Name, resultSchema.getFields().get(1).name());
        Assert.assertEquals("java.util.Date",
                AvroUtils.unwrapIfNullable(resultSchema.getFields().get(1).schema()).getProp("java-class"));
        Assert.assertEquals("yyyy-MM-dd",
                resultSchema.getFields().get(1).getProp(SchemaConstants.TALEND_COLUMN_PATTERN));


        Assert.assertEquals(field2Name, resultSchema.getFields().get(2).name());
        Assert.assertEquals("java.util.Date",
                AvroUtils.unwrapIfNullable(resultSchema.getFields().get(2).schema()).getProp("java-class"));
        Assert.assertEquals("yyyy-MM-dd",
                resultSchema.getFields().get(2).getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
    }
}