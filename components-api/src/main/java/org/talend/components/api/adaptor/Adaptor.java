package org.talend.components.api.adaptor;

import java.util.Date;

/**
 *
 */
public interface Adaptor {

    /**
     * Format the specified date according to the specified pattern.
     */
    public String formatDate(Date date, String pattern);

    /**
     * Creates a {@link ComponentDynamicHolder} object.
     */
    public ComponentDynamicHolder createDynamicHolder();

}
