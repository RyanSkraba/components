package org.talend.components.salesforce.soql;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;

/**
 * This is used tu build the SOQL query.
 */
public class SoqlQueryBuilder {

    /**
     * {@link java.lang.String} constants which are use to build SQOL query
     */
    private static final String SELECT_STATEMENT = "SELECT";

    private static final String FROM_CLAUSE = "FROM";

    private static final String SPACE_SEPARATOR = " ";

    private static final String COMMA = ",";

    private static final String DOT = ".";

    private static final String LEFT_PARENTHESIS = "(";

    private static final String RIGHT_PARENTHESIS = ")";

    private static final String UNDERSCORE = "_";

    private static final String DOUBLE_QUOTE = "\"";

    private static final String CUSTOM_FIELD_SUFFIX = "__c";

    private static final String RECORDS = "records";

    private static final int SKIP_SUBENTITY_AND_RECORDS_INDEX = 2;

    /**
     * Required field {@link org.apache.avro.Schema} schema
     */
    private Schema schema;

    /**
     * Required field {@link java.lang.String} entityName
     */
    private String entityName;

    public SoqlQueryBuilder(Schema schema, String entityName) {
        this.schema = schema;
        this.entityName = entityName;
    }

    /**
     * This method is used to build SOQL query.
     *
     * @return {@link java.lang.String} This returns SOQL query.
     */
    public String buildSoqlQuery() {
        StringBuilder resultQuery = new StringBuilder();
        List<String> complexFields = new ArrayList<>();

        resultQuery.append(DOUBLE_QUOTE).append(SELECT_STATEMENT).append(SPACE_SEPARATOR);

        for (Schema.Field field : schema.getFields()) {
            String fieldName = field.name();
            if (isCustomField(fieldName)) {
                resultQuery.append(fieldName).append(COMMA).append(SPACE_SEPARATOR);
            } else if (isChildField(fieldName)) {
                complexFields.add(fieldName);
            } else if (isParentField(fieldName)) {
                resultQuery.append(fieldName.replace('_', '.')).append(COMMA).append(SPACE_SEPARATOR);
            } else {
                resultQuery.append(fieldName).append(COMMA).append(SPACE_SEPARATOR);
            }
        }

        resultQuery.delete(resultQuery.length() - 2, resultQuery.length());

        if (!complexFields.isEmpty()) {
            resultQuery.append(COMMA).append(SPACE_SEPARATOR);
            buildSubquery(complexFields, resultQuery);
        }

        resultQuery.append(SPACE_SEPARATOR).append(FROM_CLAUSE).append(SPACE_SEPARATOR).append(entityName);

        return resultQuery.append(DOUBLE_QUOTE).toString();
    }

    /**
     * This method is used to build SOQL subquery.
     *
     * @param inputStrings {@link java.util.List} is the list of child fields
     * @param sb {@link java.lang.StringBuilder}
     *
     * @return {@link java.lang.String} This returns SOQL subquery.
     */
    private void buildSubquery(List<String> inputStrings, StringBuilder sb) {
        sb.append(LEFT_PARENTHESIS).append(SELECT_STATEMENT).append(SPACE_SEPARATOR);

        for (String item : inputStrings) {
            String[] array = item.split(UNDERSCORE);
            if (array.length == 3) {
                sb.append(array[SKIP_SUBENTITY_AND_RECORDS_INDEX]).append(COMMA).append(SPACE_SEPARATOR);
            } else {
                for (int i = SKIP_SUBENTITY_AND_RECORDS_INDEX; i < array.length; i++) {
                    sb.append(array[i]).append(DOT);
                }
                sb.delete(sb.length() - 1, sb.length());
                sb.append(COMMA).append(SPACE_SEPARATOR);
            }
        }

        sb.delete(sb.length() - 2, sb.length());

        sb.append(SPACE_SEPARATOR).append(FROM_CLAUSE).append(SPACE_SEPARATOR).append(inputStrings.get(0).split(UNDERSCORE)[0])
                .append(RIGHT_PARENTHESIS);
    }

    /**
     * Checks whether {code}fieldName{/code} is name of custom field. Custom fields end with "__c" string
     * 
     * @param fieldName from {@link org.apache.avro.Schema}
     * @return <code>true</code> when field is custom otherwise <code>false</code>
     */
    private boolean isCustomField(String fieldName) {
        return fieldName.endsWith(CUSTOM_FIELD_SUFFIX);
    }

    /**
     * Checks whether {code}fieldName{/code} is name of child field. Child fields contain "_records_" string
     *
     * @param fieldName from {@link org.apache.avro.Schema}
     * @return <code>true</code> when field is child otherwise <code>false</code>
     */
    private boolean isChildField(String fieldName) {
        return fieldName.contains(UNDERSCORE) && fieldName.contains(RECORDS);
    }

    /**
     * Checks whether {code}fieldName{/code} is name of parent field. Parent fields contain "_" string
     *
     * @param fieldName from {@link org.apache.avro.Schema}
     * @return <code>true</code> when field is parent otherwise <code>false</code>
     */
    private boolean isParentField(String fieldName) {
        return fieldName.contains(UNDERSCORE) && !fieldName.contains(RECORDS) && !isCustomField(fieldName);
    }
}
