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
package org.talend.components.azurestorage;

import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;

public class TAzureStorageConnectionTestIT extends AzureStorageBaseTestIT {

    public TAzureStorageConnectionTestIT() {
        super("connection");
    }

    @SuppressWarnings("rawtypes")
    @Test(expected = Exception.class)
    public void testInvalidConnection() throws Throwable {
        TAzureStorageContainerListProperties properties = new TAzureStorageContainerListProperties("test");
        properties.dieOnError.setValue(true);
        BoundedReader reader = createBoundedReader(properties);
        reader.start();
    }
}
