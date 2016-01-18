package org.talend.components.mysql.tMysqlConnection;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.PropertyFactory;

/**
 * Created by bchen on 16-1-18.
 */
public class tMysqlConnectionProperties extends ComponentProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public tMysqlConnectionProperties(String name) {
        super(name);
    }

    public Property HOST = PropertyFactory.newString("HOST");
    public Property PORT = PropertyFactory.newString("PORT");
    public Property DBNAME = PropertyFactory.newString("DBNAME");
    public Property PROPERTIES = PropertyFactory.newString("PROPERTIES", "noDatetimeStringSync=true");
    public Property USER = PropertyFactory.newString("USER");
    public Property PASS = PropertyFactory.newString("PASS");


}