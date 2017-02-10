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

import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

/**
 * Class RemoteBlobsGetTable.
 *
 * Same as {@link RemoteBlobsTable} with an additional parameter <code>create</code>.
 */
public class RemoteBlobsGetTable extends RemoteBlobsTable {

    private static final long serialVersionUID = -5346899079291825621L;

    /** create - Create parent directories. */
    public Property<List<Boolean>> create = newProperty(LIST_BOOLEAN_TYPE, "create"); //$NON-NLS-1$

    /**
     * Instantiates a new RemoteBlobsGetTable(String name).
     *
     * @param name {@link String} name
     */
    public RemoteBlobsGetTable(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(prefix);
        mainForm.addColumn(include);
        mainForm.addColumn(create);
    }
}
