// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * Unit tests for {@link SnowflakeCloseSourceOrSink} class
 */
public class SnowflakeCloseSourceOrSinkTest {

    SnowflakeCloseSourceOrSink closeSourceOrSink;

    @Before
    public void reset() {
        closeSourceOrSink = new SnowflakeCloseSourceOrSink();
        closeSourceOrSink.initialize(null, new TSnowflakeCloseProperties("connection"));
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
    public void testValidate() throws SQLException {
        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        Connection connection = Mockito.mock(Connection.class);
        Mockito.doNothing().when(connection).close();
        Mockito.when(container.getComponentData(Mockito.anyString(), Mockito.anyString())).thenReturn(connection);

        Assert.assertEquals(ValidationResult.Result.OK, closeSourceOrSink.validate(container).getStatus());
    }

    @Test
    public void testValidateFailedToClose() throws SQLException {
        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        Connection connection = Mockito.mock(Connection.class);
        Mockito.doThrow(new SQLException("Failed to close connection")).when(connection).close();
        Mockito.when(container.getComponentData(Mockito.any(), Mockito.any())).thenReturn(connection);

        Assert.assertEquals(ValidationResult.Result.ERROR, closeSourceOrSink.validate(container).getStatus());
    }

    @Test
    public void testValidateWithEmptyContainer() {
        ValidationResult result;

        result = closeSourceOrSink.validate(null);

        assertEquals(result.getStatus(), ValidationResult.OK.getStatus());
    }

}
