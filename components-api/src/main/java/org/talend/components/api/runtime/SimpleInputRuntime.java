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
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class SimpleInputRuntime<OutputObject> implements BaseRuntime {

    private SingleOutputConnector<OutputObject> soc;

    /**
     * This must be set by the runtime engine facet implmentation
     * 
     * @param soc connector used to ouput the data for the Input facet.
     */
    public void setOutputConnector(SingleOutputConnector<OutputObject> soc) {
        this.soc = soc;
    }

    /**
     * called to create all the inputs values they should all be outputed using the
     * {@link SimpleInputRuntime#addToMainOutput(Object)}
     *
     * @throws Exception
     */
    public abstract void execute() throws Exception;

    public void addToMainOutput(OutputObject output) {
        soc.outputMainData(output);
    }

}
