package org.talend.components.common.tableaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class TableAction {

    public static enum TableActionEnum {
        NONE,
        DROP_CREATE,
        CREATE,
        CREATE_IF_NOT_EXISTS,
        DROP_IF_EXISTS_AND_CREATE,
        CLEAR,
        TRUNCATE
    }

    private TableActionConfig config = new TableActionConfig();

    /**
     *
     * @return List<String> List of queries to execute.
     */
    public abstract List<String> getQueries() throws Exception;

    public void setConfig(TableActionConfig config){
        this.config = config;
    }

    public TableActionConfig getConfig(){
        return this.config;
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
