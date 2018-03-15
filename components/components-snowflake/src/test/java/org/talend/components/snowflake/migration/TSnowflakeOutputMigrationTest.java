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
package org.talend.components.snowflake.migration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.talend.components.snowflake.TestUtils;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer.Deserialized;

public class TSnowflakeOutputMigrationTest {

    @Test
    public void testSnowflakeConvertToUppercaseMigration() throws IOException {
        Deserialized<TSnowflakeOutputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSnowflakeOuputProperties.json"),
                TSnowflakeOutputProperties.class);

        assertTrue("The property should be migrated, the migration returned false instead of true", deser.migrated);
        TSnowflakeOutputProperties properties = deser.object;
        boolean convertToUppercase = properties.convertColumnsAndTableToUppercase.getValue();
        assertFalse(convertToUppercase);
    }

}
