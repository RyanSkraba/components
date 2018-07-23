// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.helpers;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TokenTable extends ComponentPropertiesImpl {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {// empty
    };

    public Property<List<String>> tokenName = newProperty(LIST_STRING_TYPE, "tokenName");

    public Property<List<String>> tokenValue = newProperty(LIST_STRING_TYPE, "tokenValue");

    public TokenTable(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(tokenName);
        mainForm.addColumn(tokenValue);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    public Map<String, String> getTokens() {
        Map<String, String> result = new HashMap<>();
        List<String> values = tokenName.getValue();
        if (values == null || values.isEmpty()) {
            return result;
        }
        for (int i = 0; i < values.size(); i++) {
            String tokenName = this.tokenName.getValue().get(i);
            String tokenValue = this.tokenValue.getValue().get(i);
            if (tokenValue == null || tokenValue.isEmpty()) {
                tokenValue = tokenName;
            }
            result.put(tokenName, tokenValue);
        }
        return result;
    }

    public JsonElement getTokensAsJson() {
        List<JsonObject> result = new ArrayList<>();
        List<String> values = tokenName.getValue();
        if (values == null || values.isEmpty()) {
            return new Gson().toJsonTree(result);
        }
        for (int i = 0; i < values.size(); i++) {
            String tokenName = this.tokenName.getValue().get(i);
            String tokenValue = this.tokenValue.getValue().get(i);
            JsonObject resultj = new JsonObject();
            resultj.addProperty("name", this.tokenName.getValue().get(i));
            resultj.addProperty("value", this.tokenValue.getValue().get(i));
            result.add(resultj);
        }
        return new Gson().toJsonTree(result);
    }

    public int size() {
        if (tokenName.getValue() == null) {
            return 0;
        }
        return tokenName.getValue().size();
    }

}
