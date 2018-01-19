package org.talend.components.jdbc.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLTextUtils {

    private static final String JAVA_NEW_CONTEXT_PREFIX = "context.";

    private static final String QUOTE_PATTERN = "((?<!\\\\)" + IQueryGenerator.JAVA_TEXT_FENCE + ".*?(?<!\\\\)"
            + IQueryGenerator.JAVA_TEXT_FENCE + ")";

    private static Pattern javaLiteralPatternRegex = Pattern.compile(QUOTE_PATTERN, Pattern.CANON_EQ | Pattern.MULTILINE);

    private static final String CONTEXT_AND_VARIABLE_PATTERN = "^[a-zA-Z\\_]+[a-zA-Z0-9\\_]*$";

    private static final String SINGLE_QUOTE = "'";

    private static final String ANTI_QUOTE = "`";

    private static final String QUOTATION_MARK = "\"";

    private static final String QUOTATION_ESC_MARK = "\"";

    private static final String LBRACKET = "[";

    private static final String RBRACKET = "]";

    public static String getStringDeclare() {
        return "\"";
    }

    public static String getStringConnect() {
        return "+";
    }

    private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList("abstract", "continue", "for", "new",
            "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break",
            "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum",
            "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface",
            "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while"));

    public static boolean containContextVariables(String str) {
        if (str == null) {
            return false;
        }
        str = str.trim();
        String nonQuoteStr = filterQuote(str);
        return getVariableFromCode(nonQuoteStr) != null;
    }

    public static String removeJavaTextFence(String text) {
        if (text == null) {
            return null;
        }

        return removeQuotes(text, IQueryGenerator.JAVA_TEXT_FENCE);
    }

    //expect result :
    //"[objectnmae]"
    //"[" + var + "]"
    public static String addSQLTextFenceByDbType(String objectName, EDatabaseTypeName dbType, boolean simple) {
        final String sqlTextFence = getDatabaseObjectTextFenceByDBType(dbType, true);
        return addSQLTextFence(objectName, sqlTextFence, simple);
    }

    private static String addSQLTextFence(String text, String sqlTextFence, boolean simple) {
        String con = getStringConnect();
        String newString;
        if (simple) {
            text = removeQuotes(text, getStringDeclare());
        }

        if (sqlTextFence.equals(SINGLE_QUOTE)) {
            if (simple) {
                newString = declareString(SINGLE_QUOTE + text + SINGLE_QUOTE);
            } else {
                newString = declareString(SINGLE_QUOTE) + con + text + con + declareString(SINGLE_QUOTE);
            }
        } else if (sqlTextFence.equals(ANTI_QUOTE)) {
            if (simple) {
                newString = declareString(ANTI_QUOTE + text + ANTI_QUOTE);
            } else {
                newString = declareString(ANTI_QUOTE) + con + text + con + declareString(ANTI_QUOTE);
            }
        } else if (sqlTextFence.equals(LBRACKET) || sqlTextFence.equals(RBRACKET)) {
            if (simple) {
                newString = declareString(LBRACKET + text + RBRACKET);
            } else {
                newString = declareString(LBRACKET) + con + text + con + declareString(RBRACKET);
            }
        } else {
            if (simple) {
                newString = declareString("\\" + QUOTATION_MARK + text + "\\" + QUOTATION_MARK);
            } else {
                newString = declareString("\\" + QUOTATION_MARK) + con + text + con + declareString("\\" + QUOTATION_MARK);
            }
        }
        return newString;
    }

    public static String declareString(String input) {
        if (input == null) {
            return null;
        }

        return getStringDeclare() + input + getStringDeclare();
    }

    public static String getDatabaseObjectTextFenceByDBType(EDatabaseTypeName name,
            boolean left/*
                         * sometimes, the text fence is different between left and right like [], so the left is [, the right is ]
                         */) {

        switch (name) {
        case GODBC:
            return QUOTATION_MARK;
        case IBMDB2:
            return QUOTATION_MARK;
        case INGRES:
            return QUOTATION_MARK;
        case MSODBC:
            return QUOTATION_MARK;
        case MYSQL:
        case AMAZON_AURORA:
            return ANTI_QUOTE;
        case ORACLEFORSID:
            return QUOTATION_MARK;
        case ORACLESN:
            return QUOTATION_MARK;
        case PSQL:
        case GREENPLUM:
        case PARACCEL:
        case PLUSPSQL:
            return QUOTATION_MARK;
        case SYBASEASE:
            return QUOTATION_MARK;
        case SYBASEIQ:
            return QUOTATION_MARK;
        case INTERBASE:
            return QUOTATION_MARK;
        case SQLITE:
            return QUOTATION_MARK;
        case FIREBIRD:
            return QUOTATION_MARK;
        case INFORMIX:
            return QUOTATION_MARK;
        case MSSQL:
        case ACCESS:
            return getBracket(left);
        case TERADATA:
            return QUOTATION_MARK;
        case H2:
            return QUOTATION_ESC_MARK;
        default:
            return QUOTATION_MARK;
        }
    }

    private static String getBracket(boolean left) {
        if (left) {
            return LBRACKET;
        } else {
            return RBRACKET;
        }
    }

    private static String removeQuotes(String text, String quotation) {
        if (text == null) {
            return null;
        }
        if (text.length() > 1) {
            String substring = text.substring(0, 1);
            if (quotation.equals(substring)) {
                text = text.substring(1, text.length());
            }
            substring = text.substring(text.length() - 1, text.length());
            if (quotation.equals(substring)) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text;
    }

    private static String filterQuote(final String str) {
        String newStr = replaceNewLine(str);

        Matcher regexMatcher = javaLiteralPatternRegex.matcher(newStr);
        String nonQuoteStr = newStr;
        if (regexMatcher.find()) {
            String quoteStr = regexMatcher.group(1);
            int index = newStr.indexOf(quoteStr);
            nonQuoteStr = newStr.substring(0, index);
            nonQuoteStr += newStr.substring(index + quoteStr.length());
            return filterQuote(nonQuoteStr);

        }
        return nonQuoteStr;
    }

    private static String getVariableFromCode(String code) {
        return getVariableFromCode(code, JAVA_NEW_CONTEXT_PREFIX);
    }

    private static String getVariableFromCode(String code, String prefix) {
        if (code == null) {
            return null;
        }
        String pattern = null;
        String varPattern = "(.+?)";
        String wordPattern = "\\b";
        pattern = wordPattern + replaceCharForRegex(prefix) + varPattern + wordPattern;
        if (pattern != null) {
            Pattern regex = Pattern.compile(pattern, Pattern.CANON_EQ);
            Matcher regexMatcher = regex.matcher(code);
            if (regexMatcher.find()) {
                try {
                    String var = regexMatcher.group(1);
                    if (var != null && isValidParameterName(var)) {
                        return var;
                    }
                } catch (RuntimeException re) {
                    // not match
                }
            }
        }
        return null;
    }

    private static boolean isValidParameterName(String name) {
        if (name != null) {
            // for java, the var name not be named with java keywords.
            if (isJavaKeyWords(name)) {
                return false;
            }
            return Pattern.matches(CONTEXT_AND_VARIABLE_PATTERN, name);
        }
        return false;
    }

    private static boolean isJavaKeyWords(String name) {
        return name == null ? false : JAVA_KEYWORDS.contains(name.toLowerCase());
    }

    private static String replaceCharForRegex(String pattern) {
        if (pattern == null) {
            return null;
        }
        pattern = pattern.replaceAll("\\(", "\\\\(");
        pattern = pattern.replaceAll("\\)", "\\\\)");

        pattern = pattern.replaceAll("\\.", "\\\\.");

        int index = pattern.indexOf("$");
        if (index > -1) {
            String str1 = pattern.substring(0, index);
            String str2 = pattern.substring(index + 1);
            pattern = str1 + "\\$" + str2;

        }
        return pattern;
    }

    private static String replaceNewLine(final String str) {
        if (str == null) {
            return "";
        }
        String newStr = str;

        newStr = newStr.replace("\r", " ");
        newStr = newStr.replace("\n", " ");
        newStr = newStr.trim();

        return newStr;
    }

}
