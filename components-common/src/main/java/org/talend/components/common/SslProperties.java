package org.talend.components.common;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class SslProperties extends PropertiesImpl {

    public Property<Boolean> useSsl = PropertyFactory.newBoolean("useSsl", false);

    public EnumProperty<StoreType> trustStoreType = PropertyFactory.newEnum("trustStoreType", StoreType.class);

    public Property<String> trustStorePath = PropertyFactory.newString("trustStorePath");

    public Property<String> trustStorePassword = PropertyFactory.newString("trustStorePassword");

    public Property<Boolean> needClientAuth = PropertyFactory.newBoolean("needClientAuth", false);

    public EnumProperty<StoreType> keyStoreType = PropertyFactory.newEnum("keyStoreType", StoreType.class);

    public Property<String> keyStorePath = PropertyFactory.newString("keyStorePath");

    public Property<String> keyStorePassword = PropertyFactory.newString("keyStorePassword");

    // If verify client hostname with the hostname in the cert. for debugging, usually set false
    public Property<Boolean> verifyHost = PropertyFactory.newBoolean("verifyHost", true);

    public SslProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form main = new Form(this, Form.MAIN);
        main.addRow(useSsl);
        main.addRow(trustStoreType);
        main.addRow(trustStorePath);
        main.addRow(Widget.widget(trustStorePassword).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        main.addRow(needClientAuth);
        main.addRow(keyStoreType);
        main.addRow(keyStorePath);
        main.addRow(Widget.widget(keyStorePassword).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        main.addRow(verifyHost);
    }

    public void afterUseSsl() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterNeedClientAuth() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {

            form.getWidget(trustStoreType).setVisible(useSsl);
            form.getWidget(trustStorePath).setVisible(useSsl);
            form.getWidget(trustStorePassword).setVisible(useSsl);
            form.getWidget(needClientAuth).setVisible(useSsl);
            form.getWidget(verifyHost).setVisible(useSsl);

            boolean needClient = useSsl.getValue() && needClientAuth.getValue();
            form.getWidget(keyStoreType).setVisible(needClient);
            form.getWidget(keyStorePath).setVisible(needClient);
            form.getWidget(keyStorePassword).setVisible(needClient);

        }
    }

    public enum StoreType {
        JKS,
        PKCS12
    }
}
