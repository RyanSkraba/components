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
package org.talend.components.api.test.testcomponent;

import static org.talend.components.api.schema.SchemaFactory.newProperty;

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class TestComponentProperties extends ComponentProperties {

    public static class NestedComponentProperties extends ComponentProperties {

        public static final String A_GREAT_PROP_NAME = "aGreatProp"; //$NON-NLS-1$

        public SchemaElement aGreatProperty = newProperty(A_GREAT_PROP_NAME);

        public NestedComponentProperties(I18nMessageProvider messageProvider) {
            super(messageProvider, "org.talend.components.api.test.testcomponent.nestedMessage"); //$NON-NLS-1$
            setupPropertiesWithI18n();// this must be called once each property instance is created
        }

    }

    public static final String USER_ID_PROP_NAME = "userId"; //$NON-NLS-1$

    public SchemaElement userId = newProperty(USER_ID_PROP_NAME).setRequired(true);

    public SchemaElement password = newProperty("password").setRequired(true);

    public NestedComponentProperties nestedProps;

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties() {
        super(null, ""); //$NON-NLS-1$
    }

    public TestComponentProperties(I18nMessageProvider i18nMessageProvider) {
        super(i18nMessageProvider, "org.talend.components.api.test.testcomponent.testMessage"); //$NON-NLS-1$
        Form form = Form.create(this, TESTCOMPONENT, "Test Component");
        form.addRow(userId);
        form.addRow(password);
        nestedProps = new NestedComponentProperties(i18nMessageProvider);
        setupPropertiesWithI18n();// this must once each property instance is created
    }
}
