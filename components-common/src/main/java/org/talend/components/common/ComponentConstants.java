package org.talend.components.common;

/**
 * Constants to be used to augment the Avro schema.
 */
public class ComponentConstants {

    /**
     * Use this property to specify number format thousands separator
     */
    public final static String THOUSANDS_SEPARATOR = "thousands.separator";

    /**
     * Use this property to specify number format decimal separator.
     */
    public final static String DECIMAL_SEPARATOR = "decimal.separator";

    /**
     * Use this property to specify data parser encoding.
     */
    public final static String CHARSET_NAME = "charset.name";

    /**
     * Use this property to know ignore or throw the exception during runtime.
     */
    public final static String DIE_ON_ERROR = "component.dieonerror";

    /**
     * Specify whether or not date/time parsing is to be lenient.
     * "true" means strict, "false" means lenient
     */
    public final static String CHECK_DATE = "check.date";

}
