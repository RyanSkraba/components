package org.talend.component.common;

import org.talend.component.ComponentProperties;
import org.talend.component.properties.Property;
import org.talend.component.properties.layout.Layout;

public class OauthProperties extends ComponentProperties {

    public Property<String> clientId = new Property<String>("clientId", "Client Id", null, true);

    public Property<String> clientSecret = new Property<String>("clientSecret", "Client Secret", null, true);

    public Property<String> callbackHost = new Property<String>("callbackHost", "Callback Host", null, true);

    public Property<Integer> callbackPort = new Property<Integer>("callbackPort", "Callback Port", null, true);

    public Property<String> tokenFile = new Property<String>("tokenFile", "Token File", null, true);

    public OauthProperties() {
        clientId.setLayout(Layout.create().setRow(0));
        clientSecret.setLayout(Layout.create().setRow(1));
        callbackHost.setLayout(Layout.create().setRow(2));
        callbackPort.setLayout(Layout.create().setRow(3));
        tokenFile.setLayout(Layout.create().setRow(4));
    }

}
