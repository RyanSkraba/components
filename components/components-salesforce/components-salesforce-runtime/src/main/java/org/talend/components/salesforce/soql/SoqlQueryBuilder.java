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
import java.util.List;
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

    private static final String CUSTOM_FIELD_SUFFIX = "__c";

    private static final String RELATION_ENTITY_SUFFIX = "__r";

    private static final Pattern PATTERN = Pattern.compile("\\w+(__[rc]_)\\w+");

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
     * <code>SELECT Id, Name, person_age__c FROM person__c</code></li>
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
        List<String> complexFields = new ArrayList<>();
        // This variable shows, where need to insert the sub query.
        int childPosition = 0;
        resultQuery.append(DOUBLE_QUOTE).append(SELECT_STATEMENT).append(SPACE_SEPARATOR);

        for (Schema.Field field : schema.getFields()) {
            String fieldName = field.name();
            if (isChildField(fieldName)) {
                // Catching first child field position in result query.
                if (0 == childPosition) {
                    childPosition = resultQuery.length();
                }
                complexFields.add(fieldName);
            } else if (PATTERN.matcher(fieldName).matches()) {// Check if field has any relations with custom fields.
                resultQuery.append(splitParentCustomField(fieldName)).append(COMMA_AND_SPACE);
            } else if (fieldName.contains(UNDERSCORE) && !isCustomValues(fieldName)) {// Other relations without custom fields.
                resultQuery.append(fieldName.replace('_', '.')).append(COMMA_AND_SPACE);
            } else {
                // Custom and common fields.
                resultQuery.append(fieldName).append(COMMA_AND_SPACE);
            }
        }

        // If list contains at least 1 child field, insert sub query into defined position.
        if (!complexFields.isEmpty()) {
            resultQuery.insert(childPosition, buildSubquery(complexFields).append(COMMA_AND_SPACE));
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
     * <li>custom_module__r_records_Name</li>
     * <li>custom_module__r_records_custom_name__c</li>
     * <li>custom_module__r_records_Account_Name</li>
     * </ol>
     *
     * <b> Result </b>
     *  <code>(SELECT Name, custom_name__c, Account.Name FROM custom_module__r)</code>
     *
     * @param inputStrings {@link java.util.List} is the list of child fields.
     * @return {@link java.lang.StringBuilder} SOQL sub query.
     */
    private StringBuilder buildSubquery(List<String> inputStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append(LEFT_PARENTHESIS).append(SELECT_STATEMENT).append(SPACE_SEPARATOR);

        String moduleName = null;
        for (String item : inputStrings) {
            String[] array = item.split(RECORDS);
            String columnName = null;
            //We expect only 2 values from splitting field tableName and columnName
            if ((array.length != 2) || StringUtils.isBlank(array[0]) || StringUtils.isBlank(columnName = array[1])) {
                // Should notify user about invalid table name or column name.
                TalendRuntimeException.build(SalesforceErrorCodes.INVALID_SOQL)
                        .put(ExceptionContext.KEY_MESSAGE, "Relation Parent to Child has invalid child table name or column name")
                        .throwIt();
            }
            if (null == moduleName) {
                moduleName = getSplittedValue(array[0]);
            }
            sb.append(getSplittedValue(columnName)).append(COMMA_AND_SPACE);
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(FROM_CLAUSE).append(moduleName).append(RIGHT_PARENTHESIS);
        return sb;
    }

    private String getSplittedValue(String value) {
        return PATTERN.matcher(value).matches() ? splitParentCustomField(value) : !isCustomValues(value) ? value.replaceAll(UNDERSCORE, DOT) : value;
    }

    /**
     * Checks whether <code>fieldName</code> is a name of custom field.
     * Custom values contain <code>"__c"</code> string or <code>"__r"<code>.
     *
     * @param fieldName - field name in {@link org.apache.avro.Schema}.
     * @return <code>true</code> when field is a custom, otherwise <code>false</code>.
     */
    private boolean isCustomValues(String fieldName) {
        return fieldName.contains(CUSTOM_FIELD_SUFFIX) || fieldName.contains(RELATION_ENTITY_SUFFIX);
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
     * Finds all custom values in <code>fieldName</code> and replace "_" between them with ".",<br/>
     * if the last field is not a custom, split all "_" with ".".<br/>
     * Example:<br/>
     * <b>Schema</b> contains such fields:
     *      <ol>
     *          <li><code>custom_module__r_custom_table__c_custom_name__c</code></li>
     *          <li><code>custom_module__r_custom_table__c_Person_Name</code></li>
     *      </ol>
     * <b> Result </b> must be this one:
     *      <ol>
     *          <li><code>custom_module__r.custom_table__c.custom_name__c</code></li>
     *          <li><code>custom_module__r.custom_table__c.Person.Name</code></li>
     *      </ol>
     *
     * @param fieldName - field name in {@link org.apache.avro.Schema}.
     * @return replaced input fieldName with "." instead of "_" where needed.
     */
    private String splitParentCustomField(String fieldName) {
        StringBuilder sb = new StringBuilder(fieldName);
        Matcher matcher = PATTERN.matcher(fieldName);
        int lastPostition = 0;
        matcher.find();
        do {
            sb.replace(matcher.end(1) - 1, matcher.end(1), DOT);
            lastPostition = matcher.end(1);
        } while (matcher.find());

        if (!fieldName.endsWith(CUSTOM_FIELD_SUFFIX) && !fieldName.endsWith(RELATION_ENTITY_SUFFIX)) {
            String nonCustomRelationField = fieldName.substring(lastPostition).replaceAll(UNDERSCORE, DOT);
            sb.replace(lastPostition, sb.length(), nonCustomRelationField);
        }
        return sb.toString();
    }

}
