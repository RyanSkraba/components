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
package org.talend.components.dataprep.tdatasetinput;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.components.dataprep.connection.DataPrepStreamMapper;

import java.io.IOException;

/**
 *
 */
public class TestDataSetInputStreaming {

    private DataPrepConnectionHandler connectionHandler;

    public TestDataSetInputStreaming() {
        connectionHandler = new DataPrepConnectionHandler("http://localhost:9999", //
                "jixiao@dataprep.com", //
                "jixiao", //
                "f3228f00-cf17-44f7-a763-a86aed963ea5", //
                //"5bc8a19d-7d9d-4eaa-991e-b9e7cda8a941", //
                "cars");
    }

    @Test
    @Ignore
    public void shouldStreamDataSetContent() throws IOException {
        connectionHandler.connect();
        final DataPrepStreamMapper dataPrepStreamMapper = connectionHandler.readDataSetIterator();
        dataPrepStreamMapper.initIterator();
        while (dataPrepStreamMapper.hasNextRecord()) {
            System.out.println(dataPrepStreamMapper.nextRecord());
        }
        dataPrepStreamMapper.close();
        connectionHandler.logout();
    }

}
