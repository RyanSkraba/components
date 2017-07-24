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
package org.talend.components.processing.definition.normalize;

import java.util.Arrays;
import java.util.List;

public class NormalizeConstant {

    public static class Delimiter {

        public static String SEMICOLON = "SEMICOLON";

        public static String COLON = "COLON";

        public static String COMMA = "COMMA";

        public static String TABULATION = "TABULATION";

        public static String SPACE = "SPACE";

        public static String OTHER = "OTHER";
    }

    public static final List<String> FIELD_SEPARATORS = Arrays.asList(Delimiter.SEMICOLON, Delimiter.COLON, Delimiter.COMMA,
            Delimiter.TABULATION, Delimiter.SPACE, Delimiter.OTHER);
}
