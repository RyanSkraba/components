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
package org.talend.components.processing.definition.filterrow;

import java.util.Arrays;
import java.util.List;

public class ConditionsRowConstant {

    public static class Function {

        public static String EMPTY = "EMPTY";

        public static String ABS_VALUE = "ABS_VALUE";

        public static String LOWER_CASE = "LC";

        public static String UPPER_CASE = "UC";

        public static String FIRST_CHARACTER_LOWER_CASE = "LCFIRST";

        public static String FIRST_CHARACTER_UPPER_CASE = "UCFIRST";

        public static String LENGTH = "LENGTH";

    }

    public static class Operator {

        public static String EQUAL = "==";

        public static String NOT_EQUAL = "!=";

        public static String LOWER = "<";

        public static String LOWER_OR_EQUAL = "<=";

        public static String GREATER = ">";

        public static String GREATER_OR_EQUAL = ">=";

        public static String MATCH = "MATCH";

        public static String NOT_MATCH = "NOT_MATCH";

        public static String CONTAINS = "CONTAINS";

        public static String NOT_CONTAINS = "NOT_CONTAINS";
    }

    /** List of functions that can be applied to the numerical data type **/
    public static final List<String> NUMERICAL_FUNCTIONS = Arrays.asList(Function.EMPTY, Function.ABS_VALUE);

    /** List of functions that can be applied only to the String data type **/
    public static final List<String> STRING_FUNCTIONS = Arrays.asList(Function.EMPTY, Function.LOWER_CASE, Function.UPPER_CASE,
            Function.FIRST_CHARACTER_LOWER_CASE, Function.FIRST_CHARACTER_UPPER_CASE, Function.LENGTH);

    /**
     * Default list of functions that can be applied to any data type Currently, this list only support "EMPTY", which
     * implies that no function is available
     **/
    public static final List<String> DEFAULT_FUNCTIONS = Arrays.asList(Function.EMPTY);

    /** List containing all the function, currently used until we provide the schema to the component **/
    public static final List<String> ALL_FUNCTIONS = Arrays.asList(Function.EMPTY, Function.ABS_VALUE, Function.LOWER_CASE,
            Function.UPPER_CASE, Function.FIRST_CHARACTER_LOWER_CASE, Function.FIRST_CHARACTER_UPPER_CASE, Function.LENGTH);

    /** Default list of operators that can be used to compare data **/
    public static final List<String> DEFAULT_OPERATORS = Arrays.asList(Operator.EQUAL, Operator.NOT_EQUAL, Operator.LOWER,
            Operator.GREATER, Operator.LOWER_OR_EQUAL, Operator.GREATER_OR_EQUAL, Operator.MATCH, Operator.NOT_MATCH,
            Operator.CONTAINS, Operator.NOT_CONTAINS);

    /**
     * Restricted list of operators that can be used when the function is either Function.MATCH or Function.CONTAINS. In
     * these cases, you just want to know if the result is valid or not.
     **/
    public static final List<String> RESTRICTED_OPERATORS = Arrays.asList(Operator.EQUAL, Operator.NOT_EQUAL);
}
