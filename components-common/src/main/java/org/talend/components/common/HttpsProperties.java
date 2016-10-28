package org.talend.components.common;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class HttpsProperties extends PropertiesImpl {

    public Property<Boolean> useHttps = PropertyFactory.newBoolean("useHttps", false);

    public EnumProperty<StoreType> trustStoreType = PropertyFactory.newEnum("trustStoreType", StoreType.class);

    public Property<String> trustStorePath = PropertyFactory.newString("trustStorePath");

    public Property<String> trustStorePassword = PropertyFactory.newString("trustStorePassword");

    public Property<Boolean> needClientAuth = PropertyFactory.newBoolean("needClientAuth", false);

    public EnumProperty<StoreType> keyStoreType = PropertyFactory.newEnum("keyStoreType", StoreType.class);

    public Property<String> keyStorePath = PropertyFactory.newString("keyStorePath");

    public Property<String> keyStorePassword = PropertyFactory.newString("keyStorePassword");

    // If verify client hostname with the hostname in the cert. for debugging, usually set false
    public Property<Boolean> verifyHost = PropertyFactory.newBoolean("verifyHost", true);

    public HttpsProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form main = new Form(this, Form.MAIN);
        main.addRow(useHttps);
        main.addRow(trustStoreType);
        main.addRow(trustStorePath);
        main.addRow(Widget.widget(trustStorePassword).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        main.addRow(needClientAuth);
        main.addRow(keyStoreType);
        main.addRow(keyStorePath);
        main.addRow(Widget.widget(keyStorePassword).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        main.addRow(verifyHost);
    }

    public void afterUseHttps() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterNeedClientAuth() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {

            form.getWidget(trustStoreType).setVisible(useHttps);
            form.getWidget(trustStorePath).setVisible(useHttps);
            form.getWidget(trustStorePassword).setVisible(useHttps);
            form.getWidget(needClientAuth).setVisible(useHttps);
            form.getWidget(verifyHost).setVisible(useHttps);

            boolean needClient = useHttps.getValue() && needClientAuth.getValue();
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
