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
 * Class FileMaskTable.
 *
 * A table for managing local files in TAzureStoragePutProperties.
 */
public class FileMaskTable extends ComponentPropertiesImpl {

    private static final long serialVersionUID = 703266556073143999L;

    /** LIST_STRING_TYPE. */
    protected static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    /** fileMask - file mask parameters. */
    public Property<List<String>> fileMask = newProperty(LIST_STRING_TYPE, "fileMask"); //$NON-NLS-1$

    /** newName - new name parameters. */
    public Property<List<String>> newName = newProperty(LIST_STRING_TYPE, "newName"); //$NON-NLS-1$

    /**
     * Instantiates a new FileMaskTable(String name).
     *
     * @param name {@link String} name
     */
    public FileMaskTable(String name) {
        super(name);
    }

    /**
     * convenience method for getting quickly parameters size.
     *
     * @return the size of parameters.
     */
    public int size() {
        return fileMask.getValue() == null ? 0 : fileMask.getValue().size();
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(fileMask);
        mainForm.addColumn(newName);
    }

}
