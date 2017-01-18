package org.talend.components.simplefileio.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reusable examples for CSV parsing.
 *
 * The file delimiter is ;
 */
public class CsvExample {

    /** The column values under test. */
    private final String[] values;

    /** The "canonical" line generated when writing the column to a text file. */
    private final String expectedOutput;

    /** Other possible lines that should give the same parsed values. */
    private final String[] otherInputs;

    /** List of examples to reuse. */
    private static Iterable<CsvExample> EXAMPLES = null;

    private CsvExample(String[] values, String expectedOutput, String... otherInputs) {
        this.values = values;
        this.expectedOutput = expectedOutput;
        this.otherInputs = otherInputs;
    }

    /** @return The column values under test. */
    public String[] getValues() {
        return values;
    }

    /** @return The "canonical" line generated when writing the column to a text file. */
    public String getExpectedOutputLine() {
        return expectedOutput;
    }

    /** @return Other possible lines that should give the same parsed values. */
    public List<String> getPossibleInputLines() {
        List<String> inputs = new ArrayList<>();
        inputs.add(expectedOutput);
        inputs.addAll(Arrays.asList(otherInputs));
        return inputs;
    }

    /** @return A list of examples that can be reused in unit tests. */
    public static Iterable<CsvExample> getCsvExamples() {
        if (EXAMPLES == null) {
            EXAMPLES = Arrays.asList(

            // a;b;c
                    new CsvExample(new String[] { "a", "b", "c" },
                            "a;b;c", //
                            "\"a\";b;c", "a;\"b\";c", "a;b;\"c\"", "\"a\";\"b\";c", "a;\"b\";\"c\"", "\"a\";b;\"c\"",
                            "\"a\";\"b\";\"c\""));

            // Escaped values
            new CsvExample(new String[] { "" }, "\"\"");
            new CsvExample(new String[] { ";" }, "\";\"");
            new CsvExample(new String[] { " " }, "\" \"");
            new CsvExample(new String[] { "\"" }, "\"\"\"\"");

            // Escaped values in multicolumn
            new CsvExample(new String[] { "", "", "" }, "\"\";;", "\"\";\"\";\"\"");
            new CsvExample(new String[] { ";", ";", ";" }, "\";\";\";\";\";\"");
            new CsvExample(new String[] { " ", " ", " " }, "\" \";\" \";\" \"");
            new CsvExample(new String[] { "\"", "\"", "\"" }, "\"\"\"\";\"\"\"\";\"\"\"\"");

            // No escape character
            new CsvExample(new String[] { "\\", "" }, "\\;");
            new CsvExample(new String[] { "\\", "\\", "\\" }, "\\;\\;\\");
        }

        return EXAMPLES;
    }

}
