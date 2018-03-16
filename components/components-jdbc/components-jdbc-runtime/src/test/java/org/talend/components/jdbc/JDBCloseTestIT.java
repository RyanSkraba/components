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
package org.talend.components.jdbc;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.JDBCCloseSourceOrSink;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcclose.TJDBCCloseDefinition;
import org.talend.components.jdbc.tjdbcclose.TJDBCCloseProperties;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCloseTestIT {

    private final String refComponentId = TJDBCConnectionDefinition.COMPONENT_NAME + "1";

    public static AllSetting allSetting;

    RuntimeContainer container = new RuntimeContainer() {

        private Map<String, Object> map = new HashMap<>();

        @Override
        public Object getComponentData(String componentId, String key) {
            return map.get(componentId + "_" + key);
        }

        @Override
        public void setComponentData(String componentId, String key, Object data) {
            map.put(componentId + "_" + key, data);
        }

        @Override
        public String getCurrentComponentId() {
            return refComponentId;
        }

        @Override
        public Object getGlobalData(String key) {
            return null;
        }

    };

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();
    }

    @AfterClass
    public static void afterClass() throws ClassNotFoundException, SQLException {
        DBTestUtils.shutdownDBIfNecessary();
    }

    @Test
    public void testClose() {
        TJDBCConnectionDefinition connectionDefinition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = DBTestUtils.createCommonJDBCConnectionProperties(allSetting, connectionDefinition);

        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, properties);

        ValidationResult result = sourceOrSink.validate(container);
        assertTrue(result.getStatus() == ValidationResult.Result.OK);

        TJDBCCloseDefinition closeDefinition = new TJDBCCloseDefinition();
        TJDBCCloseProperties closeProperties = (TJDBCCloseProperties) closeDefinition.createRuntimeProperties();

        closeProperties.referencedComponent.componentInstanceId.setValue(refComponentId);

        JDBCCloseSourceOrSink closeSourceOrSink = new JDBCCloseSourceOrSink();
        closeSourceOrSink.initialize(container, closeProperties);
        closeSourceOrSink.validate(container);

        try (java.sql.Connection conn = (java.sql.Connection) container.getComponentData(ComponentConstants.CONNECTION_KEY,
                refComponentId)) {
            if (conn != null) {
                Assert.assertTrue(conn.isClosed());
            }
        } catch (SQLException e) {
            Assert.fail(e.getMessage());
        }
    }

}
