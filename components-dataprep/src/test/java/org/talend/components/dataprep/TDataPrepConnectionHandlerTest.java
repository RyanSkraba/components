// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TDataPrepConnectionHandlerTest {

    private static DataPrepConnectionHandler connectionHandler;

    @BeforeClass
    public static void setConnectionHandler(){
//        connectionHandler = new DataPrepConnectionHandler("http://10.42.10.60:8888","maksym@dataprep.com","maksym");
    }

    @Test
    @Ignore
    public void validate() {
        connectionHandler = new DataPrepConnectionHandler("http://10.42.10.60:8888","maksym@dataprep.com","maksym",
                "read", "sldfjsl");
        assertTrue(connectionHandler.validate());
    }
}
