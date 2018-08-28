package org.talend.components.jdbc.validation;

import java.util.regex.Pattern;

public class PatternQueryValidator implements QueryValidator {

    private static Pattern pattern = Pattern.compile(
            "^SELECT\\s+((?!((\\bINTO\\b)|(\\bFOR\\s+UPDATE\\b)|(\\bLOCK\\s+IN\\s+SHARE\\s+MODE\\b))).)+$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    @Override
    public boolean isValid(String query) {
        return pattern.matcher(query.trim()).matches();
    }

}
