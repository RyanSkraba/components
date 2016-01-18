package org.talend.components.mysql.metadata;

import org.talend.components.api.component.runtime.metadata.Metadata;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaFactory;
import org.talend.components.mysql.type.MysqlBaseType;
import org.talend.components.mysql.type.Mysql_INT;
import org.talend.components.mysql.type.Mysql_VARCHAR;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bchen on 16-1-18.
 */
public class MysqlMetadata implements Metadata {
    private static Map<String, Class<? extends MysqlBaseType>> mapping = new HashMap<>();

    static {
        mapping.put("VARCHAR", Mysql_VARCHAR.class);
        mapping.put("INT", Mysql_INT.class);
    }


    @Override
    public void initSchema(ComponentProperties properties) {
        MysqlMetadataProperties props = (MysqlMetadataProperties) properties;
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //TODO dbproperties should not empty, need check first
        String url = "jdbc:mysql://" + props.HOST.getStringValue() + ":" + props.PORT.getStringValue() + "/" + props.DBNAME.getStringValue() + "?" + props.PROPERTIES.getStringValue();
        Connection conn;
        try {
            conn = DriverManager.getConnection(url, props.USER.getStringValue(), props.PASS.getStringValue());
            ResultSet columns = conn.getMetaData().getColumns(null, null, props.TABLE.getStringValue(), null);
            while (columns.next()) {
                props.schema.addSchemaChild(SchemaFactory.newDataSchemaElement(MysqlBaseType.FAMILY_NAME, columns.getString("COLUMN_NAME"), mapping.get(columns.getString("TYPE_NAME"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
