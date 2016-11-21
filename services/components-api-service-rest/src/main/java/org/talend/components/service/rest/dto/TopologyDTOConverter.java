package org.talend.components.service.rest.dto;

import java.beans.PropertyEditorSupport;

/**
 *
 */
public class TopologyDTOConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        final TopologyDTO dto = TopologyDTO.valueOf(text.toUpperCase());
        setValue(dto);
    }


}
