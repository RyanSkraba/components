// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.service.rest;

import java.beans.PropertyEditorSupport;

/**
 * Converter that deals with DefinitionType.
 */
public class DefinitionTypeConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        final DefinitionType definitionType = DefinitionType.valueOf(text.toUpperCase());
        setValue(definitionType);
    }


}
