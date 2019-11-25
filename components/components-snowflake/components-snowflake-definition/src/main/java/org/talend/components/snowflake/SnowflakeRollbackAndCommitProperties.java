// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;

import org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class SnowflakeRollbackAndCommitProperties extends TSnowflakeCloseProperties {

    public Property<Boolean> closeConnection = newBoolean("closeConnection");

    public SnowflakeRollbackAndCommitProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        closeConnection.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(closeConnection);
    }
}
