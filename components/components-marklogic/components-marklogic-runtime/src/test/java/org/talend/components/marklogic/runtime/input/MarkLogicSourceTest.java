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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputProperties;
import org.talend.daikon.properties.ValidationResult;

import com.marklogic.client.DatabaseClient;

public class MarkLogicSourceTest {

    private MarkLogicSource source;
    @Before
    public void setUp() {
        source = new MarkLogicSource();
    }
    @Test
    public void testGetEstimatedSizeBytes() {
        long estimatedSizeBytes = source.getEstimatedSizeBytes(null);

        assertEquals(0, estimatedSizeBytes);
    }

    @Test
    public void testProducesSortedKeys() {
        assertFalse(source.producesSortedKeys(null));
    }

    @Test
    public void testSplitIntoBundles() throws Exception {
        List<? extends BoundedSource> actualList = source.splitIntoBundles(1, null);

        assertEquals(1, actualList.size());
        assertTrue(actualList.contains(source));
    }

    @Test
    public void testCreateReader() {
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        source.initialize(null, inputProperties);
        MarkLogicCriteriaReader reader = source.createReader(null);

        assertNotNull(reader);
    }

    @Test
    public void testValidate() {
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        inputProperties.connection.referencedComponent.componentInstanceId.setValue("some value");
        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient client = mock(DatabaseClient.class);
        when(mockedContainer.getComponentData(inputProperties.connection.getReferencedComponentId(), "connection")).thenReturn(client);
        source.initialize(mockedContainer, inputProperties);

        ValidationResult result = source.validate(mockedContainer);

        assertEquals(ValidationResult.Result.OK, result.getStatus());
    }

    @Test
    public void testValidateWrongProperties() {
        MarkLogicOutputProperties wrongProperties = new MarkLogicOutputProperties("notInputProps");
        wrongProperties.init();

        source.initialize(null, wrongProperties);

        ValidationResult result = source.validate(null);


        assertEquals(ValidationResult.Result.ERROR, result.getStatus());
    }

}
