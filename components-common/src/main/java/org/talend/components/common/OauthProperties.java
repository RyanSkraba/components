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
package org.talend.components.common;

import org.talend.components.base.ComponentProperties;
import org.talend.components.base.properties.Property;
import org.talend.components.base.properties.presentation.Form;
import org.talend.components.base.properties.presentation.Layout;

public class OauthProperties extends ComponentProperties {

    public Property<String> clientId = new Property<String>("clientId", "Client Id").setRequired(true);

    public Property<String> clientSecret = new Property<String>("clientSecret", "Client Secret").setRequired(true);

    public Property<String> callbackHost = new Property<String>("callbackHost", "Callback Host").setRequired(true);

    public Property<Integer> callbackPort = new Property<Integer>("callbackPort", "Callback Port").setRequired(true);

    public Property<String> tokenFile = new Property<String>("tokenFile", "Token File").setRequired(true);

    public static final String OAUTH = "OAuth";

    public OauthProperties() {
        Form form = new Form(this, OAUTH, "OAuth Parameters");
        form.addChild(clientId, Layout.create().setRow(1).setOrder(1));
        form.addChild(clientSecret, Layout.create().setRow(1).setOrder(2));
        form.addChild(callbackHost, Layout.create().setRow(2).setOrder(1));
        form.addChild(callbackPort, Layout.create().setRow(2).setOrder(2));
        form.addChild(tokenFile, Layout.create().setRow(3));
    }

}
