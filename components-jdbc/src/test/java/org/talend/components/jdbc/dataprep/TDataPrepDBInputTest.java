// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.dataprep;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.talend.components.jdbc.dataprep.di.TDataPrepDBInputProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;

public class TDataPrepDBInputTest {

    @Test
    public void testGetSchemaNames() throws Exception {
        TDataPrepDBInputProperties properties = new TDataPrepDBInputProperties("input");
        properties.init();

        List<?> dbTypes = properties.dbTypes.getPossibleValues();
        assertTrue("the list should not be empty", dbTypes != null && !dbTypes.isEmpty());
        assertTrue("The size of list is not right", dbTypes.size() == 2);
        assertTrue("The first element is not right", "MYSQL".equals(dbTypes.get(0)));
        assertTrue("The second element is not right", "DERBY".equals(dbTypes.get(1)));

        assertTrue("The default value is not right", "MYSQL".equals(properties.dbTypes.getValue()));

        AllSetting setting = properties.getRuntimeSetting();
        assertTrue("the driver class is not right : " + setting.getDriverClass(),
                "org.gjt.mm.mysql.Driver".equals(removeQuoteIfExists(setting.getDriverClass())));
        assertTrue("the driver paths is not right : " + setting.getDriverPaths(),
                setting.getDriverPaths() != null && !setting.getDriverPaths().isEmpty()
                        && "mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.3.0"
                                .equals(removeQuoteIfExists(setting.getDriverPaths().get(0))));

        properties.dbTypes.setValue("DERBY");
        properties.afterDbTypes();
        setting = properties.getRuntimeSetting();
        assertTrue("the driver class is not right : " + setting.getDriverClass(),
                "org.apache.derby.jdbc.EmbeddedDriver".equals(removeQuoteIfExists(setting.getDriverClass())));
        assertTrue("the driver paths is not right : " + setting.getDriverPaths(),
                setting.getDriverPaths() != null && !setting.getDriverPaths().isEmpty()
                        && "mvn:org.apache.derby/derby/10.12.1.1".equals(removeQuoteIfExists(setting.getDriverPaths().get(0))));

    }

    private String removeQuoteIfExists(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (input.startsWith("\"") && input.endsWith("\"")) {
            return input.substring(1, input.length() - 1);
        }

        return input;
    }

}
