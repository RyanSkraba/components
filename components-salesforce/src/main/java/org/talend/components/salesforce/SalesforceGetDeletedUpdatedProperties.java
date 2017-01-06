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
package org.talend.components.salesforce;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class SalesforceGetDeletedUpdatedProperties extends SalesforceConnectionModuleProperties {

    /**
     * 
     */
    private static final TypeLiteral<Date> DATE_TYPE_LITERAL = new TypeLiteral<Date>() {
        // empty on purpose
    };

    public Property<Date> startDate = newProperty(DATE_TYPE_LITERAL, "startDate").setRequired();

    public Property<Date> endDate = newProperty(DATE_TYPE_LITERAL, "endDate").setRequired();

    public SalesforceGetDeletedUpdatedProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(startDate);
        mainForm.addRow(endDate);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.ADVANCED)) {
            form.getChildForm(connection.getName()).getWidget(connection.bulkConnection.getName()).setHidden(true);
            form.getChildForm(connection.getName()).getWidget(connection.httpTraceMessage.getName()).setHidden(true);
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

}
