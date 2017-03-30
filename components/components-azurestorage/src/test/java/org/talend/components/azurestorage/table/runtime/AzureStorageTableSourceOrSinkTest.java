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
package org.talend.components.azurestorage.table.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageTableSourceOrSinkTest {

    private AzureStorageTableSourceOrSink sos;

    private TAzureStorageInputTableProperties props;

    @Before
    public void setUp() throws Exception {
        sos = new AzureStorageTableSourceOrSink();
        props = new TAzureStorageInputTableProperties("test");
        props.setupProperties();
    }

    @Test
    public void testValidateEmpty() {
        sos.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, sos.validate(null).getStatus());
    }

    @Test
    public void testValidateInvalid() {
        props.tableName.setValue("10testFDDFDF");
        sos.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, sos.validate(null).getStatus());
    }

    @Ignore
    public void testValidateOK() {
        props.tableName.setValue("test");
        sos.initialize(null, props);
        assertEquals(ValidationResult.Result.OK, sos.validate(null).getStatus());
    }

}
