// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.runtime;

import java.util.List;
import java.util.Map;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.NameAndLabel;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.schema.Schema;

/**
 * Code to execute the component's runtime. This can be used at runtime or design time as required.
 */
public abstract class ComponentRuntime {

    /**
     * Sets the {@link ComponentRuntimeContainer} for this runtime.
     */
    public void setContainer(ComponentRuntimeContainer container) {
        // subclass as required
    }

    /**
     * Connect using the specified {@link ComponentProperties} returning a {@link ValidationResult}.
     *
     * Once connected, this runtime instance is bound to the connection and the other methods on it may be used.
     */
    public ValidationResult connectWithResult(ComponentProperties properties) {
        // subclass as required
        return new ValidationResult();
    }

    /**
     * Connect using the specified {@link ComponentProperties}.
     *
     * Once connected, this runtime instance is bound to the connection and the other methods on it may be used.
     */
    public void connect(ComponentProperties properties) throws Exception {
        // subclass as required
    }

    /**
     * Get the list of schema names available on the current connection. {@link #connect(ComponentProperties)} must be
     * called must be called first.
     */
    public List<NameAndLabel> getSchemaNames() throws Exception {
        // subclass as required
        return null;
    }

    /**
     * Return the schema associated with the specified schema name. {@link #connect(ComponentProperties)} must be called
     * first.
     * 
     * @param schemaName
     */
    public Schema getSchema(String schemaName) throws Exception {
        // subclass as required
        return null;
    }

    /**
     * Reads rows as specified by the {@link ComponentProperties}.
     *
     * Note this will read all of the rows into memory before returning them, so it is not necessarily suitable for
     * large amounts of data. In this case, consider using {@link #inputBegin(ComponentProperties)} and its partner
     * methods instead.
     */
    public void input(ComponentProperties props, List<Map<String, Object>> values) throws Exception {
        inputBegin(props);
        Map<String, Object> value;
        do {
            value = inputRow();
            if (value != null) {
                values.add(value);
            }
        } while (value != null);
        inputEnd();
    }

    /**
     * Initializes the input process. Used with {@link #inputRow()} to read rows.
     * 
     * @param props the {@link ComponentProperties} that are necessary to define the connection.
     * @throws Exception
     */
    public abstract void inputBegin(ComponentProperties props) throws Exception;

    /**
     * Reads a row from the input and returns it.
     *
     * {@link #inputBegin(ComponentProperties)} must be called first.
     * 
     * @return a {@link Map} of the row, null if there are no more rows
     * @throws Exception
     */
    public abstract Map<String, Object> inputRow() throws Exception;

    /**
     * Called when the input is complete to clean up.
     */
    public abstract void inputEnd() throws Exception;

    /**
     * Output the specified rows.
     *
     * Note that this is not necessarily suitable for a large number of row as this requires all of them to be in
     * memory. In this case, consider using {@link #outputBegin(ComponentProperties)} and its partner methods.
     */
    public void output(ComponentProperties props, List<Map<String, Object>> values) throws Exception {
        outputBegin(props);
        for (Map<String, Object> row : values) {
            outputMain(row);
        }
        outputEnd();
    }

    /**
     * Initialize the output process.
     * 
     * @param props the {@link ComponentProperties} that are necessary to define the connection.
     * @throws Exception
     */
    public abstract void outputBegin(ComponentProperties props) throws Exception;

    /**
     * Outputs a single row.
     * 
     * @param row the {@link Map} to be output.
     * @throws Exception
     */
    public abstract void outputMain(Map<String, Object> row) throws Exception;

    /**
     * Call when the output is complete to clean up.
     * 
     * @throws Exception
     */
    public abstract void outputEnd() throws Exception;

}
