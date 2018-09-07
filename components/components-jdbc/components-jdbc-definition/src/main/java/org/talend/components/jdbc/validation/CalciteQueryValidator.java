package org.talend.components.jdbc.validation;

import java.util.EnumSet;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.util.SqlVisitor;

public class CalciteQueryValidator implements QueryValidator {

    private static final EnumSet<SqlKind> KINDS_WHITELIST;

    static {
        KINDS_WHITELIST = EnumSet.of(SqlKind.JOIN, SqlKind.AS, SqlKind.ARGUMENT_ASSIGNMENT, SqlKind.ROW, SqlKind.TRIM,
                SqlKind.LTRIM, SqlKind.RTRIM, SqlKind.CAST);
        KINDS_WHITELIST.addAll(SqlKind.AGGREGATE);
        KINDS_WHITELIST.addAll(SqlKind.QUERY);
        KINDS_WHITELIST.addAll(SqlKind.AVG_AGG_FUNCTIONS);
        KINDS_WHITELIST.addAll(SqlKind.COMPARISON);
    }

    private final SqlVisitor<Boolean> whitelistVisitor = new SqlBasicVisitor<Boolean>() {

        public Boolean visit(SqlCall call) {
            if (!isWhitelisted(call.getKind())) {
                return false;
            }
            return super.visit(call);
        }
    };

    @Override
    public boolean isValid(final String query) {
        String checkedQuery = trimQuery(query);
        Boolean result = null;
        try {
            SqlNode parsedNode = SqlParser.create(checkedQuery).parseQuery();
            result = parsedNode.accept(whitelistVisitor);
            if (result == null) {
                result = true;
            }
        } catch (SqlParseException e) {
            return false;
        }
        return result;
    }

    private String trimQuery(final String query) {
        String result = query.trim();
        if (result.endsWith(";")) {
            int index = result.length();
            while (result.charAt(index - 1) == ';') {
                index--;
            }

            result = result.substring(0, index);
        }
        return result;
    }

    private boolean isWhitelisted(final SqlKind sqlKind) {
        return KINDS_WHITELIST.contains(sqlKind);
    }

}
