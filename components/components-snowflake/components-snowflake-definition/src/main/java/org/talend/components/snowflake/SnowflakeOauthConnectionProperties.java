// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.EnumSet;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class SnowflakeOauthConnectionProperties extends ComponentPropertiesImpl {

    private static final I18nMessages I18N =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeOauthConnectionProperties.class);

    public Property<String> oauthTokenEndpoint = newString("oauthTokenEndpoint");

    public Property<String> clientId = newString("clientId");

    public Property<String> clientSecret =
            newString("clientSecret").setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> scope = newString("scope");

    public SnowflakeOauthConnectionProperties(String name) {
        super(name);
        oauthTokenEndpoint.setRequired().setI18nMessageFormatter(I18N);
        clientId.setRequired().setI18nMessageFormatter(I18N);
        clientSecret.setRequired().setI18nMessageFormatter(I18N);
        scope.setI18nMessageFormatter(I18N);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        oauthTokenEndpoint.setValue("");
        clientId.setValue("");
        clientSecret.setValue("");
        scope.setValue("");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(widget(oauthTokenEndpoint));
        form.addRow(widget(clientId));
        form.addRow(widget(clientSecret).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        form.addRow(widget(scope));
    }

}
