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
package org.talend.components.marklogic.tmarklogicclose;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

import static org.talend.daikon.properties.presentation.Widget.widget;

public class MarkLogicCloseProperties extends ComponentPropertiesImpl {

    public MarkLogicCloseProperties(String name) {
        super(name);
    }

    public ComponentReferenceProperties<MarkLogicConnectionProperties> referencedComponent = new ComponentReferenceProperties<MarkLogicConnectionProperties>(
            "referencedComponent", MarkLogicConnectionDefinition.COMPONENT_NAME);

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = Form.create(this, Form.MAIN);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        mainForm.addRow(compListWidget);
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getValue();
    }
}
