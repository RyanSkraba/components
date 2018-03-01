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

package org.talend.components.marklogic.runtime.input;

import com.marklogic.client.DatabaseClient;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputProperties;
import org.talend.daikon.properties.ValidationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MarkLogicInputSinkTest {
    MarkLogicInputSink inputSink;

    @Before
    public void setUp() {
        inputSink = new MarkLogicInputSink();
    }
    @Test
    public void testCreateWriteOperation() {
        MarkLogicInputSink inputSink = new MarkLogicInputSink();
        MarkLogicInputWriteOperation inputWriteOperation = inputSink.createWriteOperation();

        assertNotNull(inputWriteOperation);
    }

    @Test
    public void testValidate() {
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        inputProperties.connection.referencedComponent.componentInstanceId.setValue("some value");
        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient client = mock(DatabaseClient.class);
        when(mockedContainer.getComponentData(inputProperties.connection.getReferencedComponentId(), "connection")).thenReturn(client);
        inputSink.initialize(mockedContainer, inputProperties);

        ValidationResult result = inputSink.validate(mockedContainer);

        assertEquals(ValidationResult.Result.OK, result.getStatus());
    }

    @Test
    public void testValidateWrongProperties() {
        MarkLogicOutputProperties wrongProperties = new MarkLogicOutputProperties("notInputProps");
        wrongProperties.init();

        inputSink.initialize(null, wrongProperties);

        ValidationResult result = inputSink.validate(null);


        assertEquals(ValidationResult.Result.ERROR, result.getStatus());
    }
}
