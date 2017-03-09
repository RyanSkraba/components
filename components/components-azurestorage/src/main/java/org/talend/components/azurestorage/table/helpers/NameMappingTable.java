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
package org.talend.components.azurestorage.table.helpers;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class NameMappingTable extends ComponentPropertiesImpl {

    private static final long serialVersionUID = 1978667508342861384L;

    protected static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    public Property<List<String>> schemaColumnName = newProperty(LIST_STRING_TYPE, "schemaColumnName"); //$NON-NLS-1$

    public Property<List<String>> entityPropertyName = newProperty(LIST_STRING_TYPE, "entityPropertyName"); //$NON-NLS-1$
    
    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(NameMappingTable.class);

    public NameMappingTable(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(schemaColumnName);
        mainForm.addColumn(entityPropertyName);
    }

    public int size() {
        if (schemaColumnName.getValue() != null && !schemaColumnName.getValue().isEmpty())
            return schemaColumnName.getValue().size();
        else
            return 0;
    }

    public Map<String, String> getNameMappings() {
        ValidationResult vr = validateNameMappings();
        if (vr.getStatus() == ValidationResult.Result.ERROR || size() == 0)
            return null;
        Map<String, String> result = new HashMap<>();
        for (int idx = 0; idx < schemaColumnName.getValue().size(); idx++) {
            String k = schemaColumnName.getValue().get(idx);
            String v = entityPropertyName.getValue().get(idx);
            if (k != null && !k.isEmpty() && v != null && !v.isEmpty()) {
                result.put(k, v);
            }
        }
        return result;
    }

    public ValidationResult validateNameMappings() {
        Boolean mappingOk = true;
        if (size() == 0)
            return ValidationResult.OK;
        for (int idx = 0; idx < schemaColumnName.getValue().size(); idx++) {
            String k = schemaColumnName.getValue().get(idx);
            String v = entityPropertyName.getValue().get(idx);
            mappingOk = mappingOk && (k != null && !k.isEmpty() && v != null && !v.isEmpty());
            if (!mappingOk) {
                ValidationResult vr = new ValidationResult();
                vr.setStatus(Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.MissSchemaName"));
                return vr;
            }
        }

        return ValidationResult.OK;
    }

}
