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

/**
 * Transformation interface for components having one input data and have to ouput data after a transformation either on
 * as main data or as an error object.
 */
public interface TransformationRuntime<InputObject, OutputMainObject, OutputErrorObject> extends BaseRuntime {

    /**
     * process the inputValue to output it into main or error outputs.
     *
     * @param inputValue Input value that will be processed.
     * @param outputs oubject used to ouput data.
     * @throws Exception
     */
    public abstract void execute(InputObject inputValue, DoubleOutputConnector<OutputMainObject, OutputErrorObject> outputs)
            throws Exception;

}
