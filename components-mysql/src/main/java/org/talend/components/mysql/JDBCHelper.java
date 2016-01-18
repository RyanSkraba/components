package org.talend.components.mysql;

import org.talend.components.api.component.runtime.input.Split;

import java.io.IOException;
public class JDBCHelper{

        private Manager manager;
        private java.sql.Connection connection;
        private String productName;

        public JDBCHelper(java.sql.Connection connection) throws IOException{
            try{
                this.connection = connection;
                java.sql.DatabaseMetaData dbMetaData = connection.getMetaData();
                productName = dbMetaData.getDatabaseProductName().toUpperCase();
                setManager();
            }catch(java.sql.SQLException e){
                throw new IOException(e.getMessage());
            }
        }
        
        public String getDBSql(Split split, String dbQuery, String columnsStr) throws IOException{
            return manager.getDBSql(split,dbQuery,columnsStr);
        }
        public String constructQuery(String table, String[] fieldNames){
            return manager.constructQuery(table,fieldNames);
        }
        public Manager setManager(){
            if(productName.startsWith("ORACLE")) {
                manager = new OracleManager();
            }else if(productName.startsWith("MYSQL")){
                manager = new MysqlManager();
            }else if(productName.startsWith("MICROSOFT SQL SERVER")){
                manager = new MSSQLManager();
            }else if(productName.startsWith("NETEZZA")){
                manager = new NetezzaManager();
            }else if(productName.startsWith("PARACCEL")){
                manager = new ParaccelManager();
            }else if(productName.startsWith("VERTICA")){
                manager = new VerticaManager();
            }else if(productName.startsWith("POSTGRES")){
                manager = new PostgreManager();
            }else if(productName.startsWith("TERADATA")){
                manager = new TeradataManager();
            }else if(productName.startsWith("INFORMIX")){
                manager = new InformixManager();
            }else if(productName.startsWith("DB2")){
                if(productName.indexOf("AS/400")>-1){
                    manager = new AS400Manager();
                }else{
                    manager = new DB2Manager();
                }
            }else if(productName.startsWith("INGRES")){
                manager = new IngresManager();
            }else if(productName.startsWith("SAP")){
                manager = new MaxDBManager();
            }else{
                manager = new Manager();
            }
            return manager;
        }
}


