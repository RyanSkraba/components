// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.common.BasedOnSchemaTable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class MarketoColumnMappingsTable extends BasedOnSchemaTable {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    public Property<List<String>> marketoColumnName = newProperty(LIST_STRING_TYPE, "marketoColumnName");

    private static final long serialVersionUID = 3473102423403696522L;

    public MarketoColumnMappingsTable(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        marketoColumnName.setTaggedValue(ADD_QUOTES, true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addColumn(marketoColumnName);
    }

    public Map<String, String> getNameMappingsForMarketo() {
        Map<String, String> result = new HashMap<>();
        List<String> value = columnName.getValue();
        if (value == null || value.isEmpty())
            return result;
        for (int i = 0; i < value.size(); i++) {
            String schemaCol = value.get(i);
            String marketoCol = marketoColumnName.getValue().get(i);
            if (marketoCol == null || marketoCol.isEmpty())
                marketoCol = schemaCol;
            result.put(schemaCol, marketoCol);
        }
        return result;
    }

    public int size() {
        if (marketoColumnName.getValue() == null)
            return 0;
        return marketoColumnName.getValue().size();
    }

}
