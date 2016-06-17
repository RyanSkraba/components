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
package org.talend.components.salesforce;

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.EnumSet;

import org.talend.components.common.UserPasswordProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class SalesforceUserPasswordProperties extends UserPasswordProperties {

    public Property securityKey = ((Property) newProperty("securityKey"))
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public SalesforceUserPasswordProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = getForm(Form.MAIN);
        form.addColumn(widget(securityKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));

    }

}
