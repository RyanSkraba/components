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
package org.talend.components.azurestorage.blob.helpers;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

/**
 * Class RemoteBlobsTable.
 *
 * A table for managing remote blobs prefixes (filters, selections, ...)
 */
public class RemoteBlobsTable extends ComponentPropertiesImpl {

    private static final long serialVersionUID = 9146199487401036430L;

    public static final String ADD_QUOTES = "ADD_QUOTES"; //$NON-NLS-1$

    protected static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    protected static final TypeLiteral<List<Boolean>> LIST_BOOLEAN_TYPE = new TypeLiteral<List<Boolean>>() {
    };

    /** prefix - Prefix for remote blobs parameters. */
    public Property<List<String>> prefix = newProperty(LIST_STRING_TYPE, "prefix"); //$NON-NLS-1$

    /** include - Include sub-directories parameters. */
    public Property<List<Boolean>> include = newProperty(LIST_BOOLEAN_TYPE, "include"); //$NON-NLS-1$

    /**
     * Instantiates a new RemoteBlobsTable(String name).
     *
     * @param name {@link String} name
     */
    public RemoteBlobsTable(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(prefix);
        mainForm.addColumn(include);
    }

}
