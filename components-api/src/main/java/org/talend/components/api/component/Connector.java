// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.component;

import org.apache.avro.Schema;
import org.talend.daikon.NamedThing;

/**
 * A connector is a token used by a component to define the avaialble inputs and outputs it has. It shall be used to
 * retreive the schema {@link Schema} associated with a connector if any. WARNING: this interface should not be
 * implemented by clients of the APIs, only the components will provide an implementations for it.
 */
public interface Connector extends NamedThing {

    /*
     * prefix used in I18N messages.properties file to be placed before any key related to connectors
     **/
    String I18N_PREFIX = "connector."; //$NON-NLS-1$

    /**
     * constant use to create connector with unique MAIN name
     */
    String MAIN_NAME = "MAIN"; //$NON-NLS-1$

    /**
     * constant use to create connector with unique REJECT name
     */
    String REJECT_NAME = "REJECT"; //$NON-NLS-1$

    /**
     * This method shall return a unique name for a given set of connectors so that the client can unically identify it.
     */
    @Override
    String getName();

}
