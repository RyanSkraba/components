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
package org.talend.components.marketo.runtime.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;

public class MarketoClientTestIT extends MarketoBaseTestIT {

    @BeforeClass
    public static void setupDatasets() throws Exception {
        createDatasets(50);
    }

    @AfterClass
    public static void teardownDatasets() throws Exception {
        cleanupDatasets();
    }
}
