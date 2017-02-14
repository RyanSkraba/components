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
package org.talend.components.snowflake.tsnowflakeclose;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class TSnowflakeCloseProperties extends ComponentPropertiesImpl {

    public ComponentReferenceProperties<SnowflakeConnectionProperties> referencedComponent = new ComponentReferenceProperties<SnowflakeConnectionProperties>(
            "referencedComponent", TSnowflakeConnectionDefinition.COMPONENT_NAME);

    public TSnowflakeCloseProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(refForm);
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getStringValue();
    }

}
