package org.talend.components.snowflake.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class SnowflakeCloseSourceOrSinkTest {

    SnowflakeCloseSourceOrSink closeSourceOrSink;

    @Before
    public void reset() {
        closeSourceOrSink = new SnowflakeCloseSourceOrSink();
        closeSourceOrSink.initialize(null, null);
    }

    @Test
    public void testGetSchemaNames() throws IOException {
        List<NamedThing> schemaNames;

        schemaNames = closeSourceOrSink.getSchemaNames(null);

        assertNull(schemaNames);
    }

    @Test
    public void testGetEndpointSchema() throws IOException {
        Schema endpointSchema;

        endpointSchema = closeSourceOrSink.getEndpointSchema(null, null);

        assertNull(endpointSchema);
    }

    @Test
    public void testValidate() {
        ValidationResult result;

        result = closeSourceOrSink.validate(null);

        assertEquals(result.getStatus(), ValidationResult.OK.getStatus());
    }

}
