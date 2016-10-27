/**
 *
 */
package org.talend.components.snowflake;

/**
 * @author user
 */
public interface SnowflakeProvideConnectionProperties {

    /**
     * @return the {@link SnowflakeConnectionProperties} associated with this
     * {@link org.talend.components.api.properties.ComponentProperties} object.
     */
    SnowflakeConnectionProperties getConnectionProperties();

}
