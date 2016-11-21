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
