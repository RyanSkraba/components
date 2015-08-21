package org.talend.component.common;

import org.talend.component.ComponentProperties;
import org.talend.component.properties.Property;
import org.talend.component.properties.layout.Layout;

public class ProxyProperties extends ComponentProperties {

    public Property<Boolean> useProxy = new Property<Boolean>("useProxy", "Use Proxy", false, false);

    public Property<String> host = new Property<String>("host", "Host", null, true);

    public Property<String> userName = new Property<String>("userName", "User name", null, true);

    public Property<String> password = new Property<String>("password", "Password", null, true);

    public ProxyProperties() {
        useProxy.setLayout(Layout.create().setRow(0));
        host.setLayout(Layout.create().setRow(1));
        userName.setLayout(Layout.create().setRow(2));
        password.setLayout(Layout.create().setRow(3));
    }

}
