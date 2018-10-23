package org.talend.components.common.tableaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TableAction {

    public static enum TableActionEnum {
        NONE(false),
        DROP_CREATE(true),
        CREATE(true),
        CREATE_IF_NOT_EXISTS(true),
        DROP_IF_EXISTS_AND_CREATE(true),
        CLEAR(false),
        TRUNCATE(false);

        boolean create;
        TableActionEnum(boolean create){
            this.create = create;
        }

        public boolean isCreateTableAction(){
            return create;
        }
    }

    private TableActionConfig config = new TableActionConfig();

    // Map association to define db type if SchemaConstants.TALEND_COLUMN_DB_TYPE is not set in schema
    private Map<String, String> dbTypeMap = new HashMap<>();

    /**
     *
     * @return List<String> List of queries to execute.
     */
    public abstract List<String> getQueries() throws Exception;

    public void setConfig(TableActionConfig config){
        this.config = config;
    }

    public void setDbTypeMap(Map<String, String> dbTypeMap){
        this.dbTypeMap = dbTypeMap;
    }

    public TableActionConfig getConfig(){
        return this.config;
    }

    public Map<String, String> getDbTypeMap(){
        return this.dbTypeMap;
    }

    public String escape(String value){
        if(!this.getConfig().SQL_ESCAPE_ENABLED || isEscaped(value)){
           return value;
        }

        return this.getConfig().SQL_ESCAPE+value+this.getConfig().SQL_ESCAPE;
    }

    public boolean isEscaped(String value){
        if(value.startsWith(this.getConfig().SQL_ESCAPE) && value.endsWith(this.getConfig().SQL_ESCAPE)){
            return true;
        }

        return false;
    }

    public String buildFullTableName(String[] fullTableName, String sep, boolean escape){
        StringBuilder name = new StringBuilder();
        boolean first = true;
        for(String n : fullTableName){
            if(n == null){
                continue;
            }

            if(!first){
                name.append(sep);
            }
            first = false;
            n = updateCaseIdentifier(n);
            if(escape){
                n = escape(n);
            }
            name.append(n);
        }

        return name.toString();
    }

    /**
     * Uppercase take precedence on lowercase.
     * @param identifier
     * @return The identifier in the right case.
     */
    public String updateCaseIdentifier(String identifier){
        if(this.getConfig().SQL_UPPERCASE_IDENTIFIER){
            return identifier.toUpperCase();
        }
        else if(this.getConfig().SQL_LOWERCASE_IDENTIFIER){
            return identifier.toLowerCase();
        }

        return identifier;
    }

}
