package org.talend.components.mysql.metadata;

import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.PropertyFactory;
import org.talend.components.common.SchemaProperties;
import org.talend.components.mysql.tMysqlConnection.tMysqlConnectionProperties;

/**
 * Created by bchen on 16-1-17.
 */
public class MysqlMetadataProperties extends tMysqlConnectionProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public MysqlMetadataProperties(String name) {
        super(name);
    }

    public Property TABLE = PropertyFactory.newString("TABLE");
    public SchemaProperties schema = new SchemaProperties("schema");
}
