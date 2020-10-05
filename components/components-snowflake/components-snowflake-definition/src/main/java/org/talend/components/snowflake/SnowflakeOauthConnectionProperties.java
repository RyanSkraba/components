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
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Arrays;
import java.util.EnumSet;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.migration.SerializeSetVersion;

public class SnowflakeOauthConnectionProperties extends ComponentPropertiesImpl implements SerializeSetVersion {

    private static final I18nMessages I18N =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeOauthConnectionProperties.class);

    public Property<String> oauthTokenEndpoint = newString("oauthTokenEndpoint");

    public Property<String> clientId = newString("clientId");

    public Property<String> clientSecret =
            newString("clientSecret").setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> scope = newString("scope");

    public Property<GrantType> grantType = newEnum("grantType", GrantType.class).setValue(GrantType.CLIENT_CREDENTIALS);

    public Property<String> oauthUserName = newString("oauthUserName");

    public Property<String> oauthPassword =
            newString("oauthPassword").setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public SnowflakeOauthConnectionProperties(String name) {
        super(name);
        oauthTokenEndpoint.setRequired().setI18nMessageFormatter(I18N);
        clientId.setRequired().setI18nMessageFormatter(I18N);
        clientSecret.setRequired().setI18nMessageFormatter(I18N);
        scope.setI18nMessageFormatter(I18N);
        grantType.setI18nMessageFormatter(I18N);
        oauthUserName.setRequired().setI18nMessageFormatter(I18N);
        oauthPassword.setRequired().setI18nMessageFormatter(I18N);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        oauthTokenEndpoint.setValue("");
        clientId.setValue("");
        clientSecret.setValue("");
        grantType.setValue(GrantType.CLIENT_CREDENTIALS);
        oauthUserName.setValue("");
        oauthPassword.setValue("");
        scope.setValue("");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(widget(oauthTokenEndpoint));
        form.addRow(widget(clientId));
        form.addRow(widget(clientSecret).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        form.addRow(widget(grantType));
        form.addRow(widget(oauthUserName).setHidden(true));
        form.addRow(widget(oauthPassword).setHidden(true).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        form.addRow(widget(scope));
    }

    public void afterGrantType() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        form.getWidget(oauthUserName.getName()).setHidden(grantType.getValue() == GrantType.CLIENT_CREDENTIALS);
        form.getWidget(oauthPassword.getName()).setHidden(grantType.getValue() == GrantType.CLIENT_CREDENTIALS);
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean migrated = super.postDeserialize(version, setup, persistent);
        grantType.setPossibleValues(Arrays.asList(GrantType.values()));
        return migrated;
    }

    public enum GrantType {
        CLIENT_CREDENTIALS,
        PASSWORD
    }
}
