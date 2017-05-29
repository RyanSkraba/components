// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.soql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.salesforce.common.SalesforceErrorCodes;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * This Builder is used for creating SOQL query from defined Schema and Module Name.
 */
public class SoqlQueryBuilder {

    /**
     * {@link java.lang.String} constants that are used to build SQOL query
     */
    private static final String SELECT_STATEMENT = "SELECT";

    private static final String FROM_CLAUSE = " FROM ";

    private static final String SPACE_SEPARATOR = " ";

    private static final String COMMA_AND_SPACE = ", ";

    private static final String DOT = ".";

    private static final String LEFT_PARENTHESIS = "(";

    private static final String RIGHT_PARENTHESIS = ")";

    private static final String UNDERSCORE = "_";

    private static final String DOUBLE_QUOTE = "\"";

    private static final Pattern CUSTOM_FIELD_PATTERN = Pattern.compile("[^_\\W]+(_[^_\\W])*(__[rc]){0,1}(_){0,1}");

    private static final String RECORDS = "_records_";

    /**
     * Required field {@link org.apache.avro.Schema} schema
     */
    private final Schema schema;

    /**
     * Required field {@link java.lang.String} entityName
     */
    private final String entityName;

    public SoqlQueryBuilder(Schema schema, String entityName) {
        this.schema = schema;
        this.entityName = entityName;
    }

    /**
     * This method is used to build SOQL query.
     * There are 3 possible scenarios for this method:
     * <ol>
     * <li><b>Not relational query</b><br/>
     * Each schema field represents common or custom column in module.<br/>
     * E.g.:<br/>
     * <code>SELECT Id, Name, Person_age__c FROM person__c</code></li>
     * <li><b>Parent to Child relation</b><br/>
     * Schema contains field(-s) that conform pattern<br/>
     * <code>{module_name}_records_{column_name}</code></li>
     * E.g.:<br/>
     * <code>SELECT Name, (SELECT LastName FROM Contacts) FROM Account</code>
     * <li><b>Child to Parent relation</b><br/>
     * Schema contains field(-s) that has relations such as:<br>
     * <code>{parent}_{parent_column}</code> or <code>{parent}_{AnotherRelationModule}_{column}</code><br/>
     * E.g.:<br/>
     * <code> SELECT Name, Account.Name, Account.Owner.Name FROM Contact</code></li>
     * </ol>
     *
     * @return created SOQL query.
     */
    public String buildSoqlQuery() {
        StringBuilder resultQuery = new StringBuilder();
        //It's preferable to have columns in order as in Schema.
        Map<String, List<String>> complexFields = new LinkedHashMap<>();
        // This variable shows, where need to insert the sub query.
        resultQuery.append(DOUBLE_QUOTE).append(SELECT_STATEMENT).append(SPACE_SEPARATOR);

        for (Schema.Field field : schema.getFields()) {
            String fieldName = field.name();
            if (isChildField(fieldName)) {
                String[] array = fieldName.split(RECORDS);
                String columnName = null;
                String moduleName = null;
                //We expect only 2 values from splitting field tableName and columnName
                if ((array.length != 2) || StringUtils.isBlank(moduleName = array[0]) || StringUtils.isBlank(columnName = array[1])) {
                    // Should notify user about invalid table name or column name.
                    TalendRuntimeException.build(SalesforceErrorCodes.INVALID_SOQL)
                            .put(ExceptionContext.KEY_MESSAGE, "Relation Parent to Child has invalid child table name or column name")
                            .throwIt();
                }
                List<String> columns = complexFields.get(moduleName);
                if (null == columns) {
                    columns = new ArrayList<>();
                    complexFields.put(moduleName, columns);
                }
                columns.add(columnName);
            } else {
                resultQuery.append(convertFields(fieldName)).append(COMMA_AND_SPACE);
            }
        }

        // If list contains at least 1 child field, insert sub query.
        for (Entry<String, List<String>> entry : complexFields.entrySet()) {
            resultQuery.append(buildSubquery(entry.getKey(), entry.getValue()).append(COMMA_AND_SPACE));
        }

        // Removing last comma and space from result query.
        resultQuery.delete(resultQuery.length() - 2, resultQuery.length());
        resultQuery.append(FROM_CLAUSE).append(entityName).append(DOUBLE_QUOTE);
        return resultQuery.toString();
    }

    /**
     * This method is used to build SOQL sub query for Parent to Child relation.<br/>
     * Each child field name must conform pattern <code>{module_name}_records_{column_name}</code>.<br/>
     * Where:
     * <ul>
     * <li><code>module_name</code> must be the same for each child field</li>
     * <li><code>column_name</code> may be simple, custom or relational field</li>
     * </ul>
     * Example:<br/>
     * <ol>
     * <li>module__r_records_Name</li>
     * <li>module__r_records_name__c</li>
     * <li>module__r_records_Account_Name</li>
     * </ol>
     *
     * <b> Result </b>
     *  <code>(SELECT Name, name__c, Account.Name FROM module__r)</code>
     *
     * @param inputStrings {@link java.util.List} is the list of child fields.
     * @return {@link java.lang.StringBuilder} SOQL sub query.
     */
    private StringBuilder buildSubquery(String moduleName, List<String> inputStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append(LEFT_PARENTHESIS).append(SELECT_STATEMENT).append(SPACE_SEPARATOR);

        for (String item : inputStrings) {
            sb.append(convertFields(item)).append(COMMA_AND_SPACE);
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(FROM_CLAUSE).append(convertFields(moduleName)).append(RIGHT_PARENTHESIS);
        return sb;
    }

    /**
     * Checks whether <code>fieldName</code> is a name of child field. Child fields contain "_records_" string.
     *
     * @param fieldName - field name in {@link org.apache.avro.Schema}.
     * @return <code>true</code> when field is a child, otherwise <code>false</code>.
     */
    private boolean isChildField(String fieldName) {
        return fieldName.contains(RECORDS);
    }

    /**
     * Finds all matches that conform defined pattern for possible field names.<br/>
     * Replace all needed "_" with ".". Only "__c" or "__r" won't be replaced.<br/>
     * Example:<br/>
     * <b>Schema</b> contains such fields:
     *      <ol>
     *          <li><code>Name</code></li>
     *          <li><code>Account_Name</code></li>
     *          <li><code>Account_customField__c</code></li>
     *          <li><code>Account_customTable__c_customField__c</code></li>
     *          <li><code>customTable__c_Name</code></li>
     *      </ol>
     * <b> Result </b> must be replaced with this one:
     *      <ol>
     *          <li><code>Name</code></li>
     *          <li><code>Account.Name</code></li>
     *          <li><code>Account.customField__c</code></li>
     *          <li><code>Account.customTable__c.customField__c</code></li>
     *          <li><code>customTable__c.Name</code></li>
     *      </ol>
     *
     * @param input - field name in {@link org.apache.avro.Schema}.
     * @return replaced input fieldName with "." instead of "_" where needed.
     */

    private StringBuilder convertFields(String input) {
        StringBuilder sb = new StringBuilder(input);
        Matcher matcher = CUSTOM_FIELD_PATTERN.matcher(input);
        while (matcher.find()) {
            if (-1 != matcher.start(1))
            sb.replace(matcher.start(1), matcher.end(1), matcher.group(1).replace(UNDERSCORE, DOT));
            if (-1 != matcher.start(3)) {
                sb.replace(matcher.start(3), matcher.start(3) + 1, DOT);
            }
        }

        return sb;
    }

}
