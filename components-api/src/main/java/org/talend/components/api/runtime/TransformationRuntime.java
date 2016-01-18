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

import org.talend.components.api.properties.ComponentProperties;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class TransformationRuntime<InputObject, OutputMainObject, OutputErrorObject> implements BaseRuntime {

    private DoubleOutputConnector<OutputMainObject, OutputErrorObject> doc;

    /**
     * This must be set by the runtime engine facet implmentation
     * 
     * @param doc connector used to ouput the data for the Input facet.
     */
    public void setOutputConnector(DoubleOutputConnector<OutputMainObject, OutputErrorObject> doc) {
        this.doc = doc;
    }

    public void addToMainOutput(OutputMainObject output) {
        doc.outputMainData(output);
    }

    public void addToErrorOutput(OutputErrorObject output) {
        doc.outputErrorData(output);
    }

    /**
     * process the inputValue to output it into main or error connector
     *
     * @param inputValue Input value that will be processed.
     * @throws Exception
     */
    public abstract void execute(InputObject inputValue) throws Exception;

    /**
     * default implmentation is empty
     */
    @Override
    public void tearDown() {
        // empty on purpose
    }

    /**
     * default implmentation is empty
     */
    @Override
    public void setUp(ComponentProperties comp) {
        // empty on purpose
    }

}
