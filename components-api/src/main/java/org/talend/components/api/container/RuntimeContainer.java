package org.talend.components.api.container;

import org.apache.avro.Schema;

import java.util.Date;

/**
 *
 */
public interface RuntimeContainer {

    /**
     * Format the specified date according to the specified pattern.
     */
    // FIXME - remove this
    public String formatDate(Date date, String pattern);

}
