package org.talend.components.api.component;

/**
 * Use to define product type which is supported by component.
 */
public interface SupportedProduct {

    public final static String ALL = "ALL";

    public final static String DI = "DI";

    public final static String MAP_REDUCE = "MAP_REDUCE";

    public final static String STORM = "STORM";

    public final static String SPARK = "SPARK";

    public final static String SPARKSTREAMING = "SPARKSTREAMING";

    public final static String DATAPREP = "DATAPREP";

    public final static String DATASTREAMS = "DATASTREAMS";

}
