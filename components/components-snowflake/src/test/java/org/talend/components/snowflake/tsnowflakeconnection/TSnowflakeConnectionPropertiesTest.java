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
package org.talend.components.snowflake.tsnowflakeconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.daikon.properties.presentation.Form.MAIN;

import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputDefinition;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class TSnowflakeConnectionPropertiesTest {

    @Test
    public void testConnectionProps() throws Throwable {
        SnowflakeConnectionProperties props = (SnowflakeConnectionProperties) new TSnowflakeConnectionDefinition()
                .createRuntimeProperties().init();
        assertTrue(props.userPassword.userId.isRequired());
        assertTrue(props.userPassword.password.isRequired());
        assertFalse(props.warehouse.isRequired());
        assertTrue(props.schemaName.isRequired());
        assertTrue(props.db.isRequired());
    }

    @Test
    public void testInputProps() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        assertFalse(props.manualQuery.getValue());
        Property[] returns = new TSnowflakeInputDefinition().getReturnProperties();
        assertEquals(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT, returns[1].getName());

        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(true);
        props.afterManualQuery();
        assertFalse(f.getWidget(props.query.getName()).isHidden());
        assertTrue(f.getWidget(props.condition.getName()).isHidden());

        props.manualQuery.setValue(false);
        props.afterManualQuery();
        assertTrue(f.getWidget(props.query.getName()).isHidden());
        assertFalse(f.getWidget(props.condition.getName()).isHidden());
    }

}
