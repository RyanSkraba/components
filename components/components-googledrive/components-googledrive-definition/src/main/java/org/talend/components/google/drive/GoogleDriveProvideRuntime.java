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
package org.talend.components.google.drive;

import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.properties.ValidationResult;

public interface GoogleDriveProvideRuntime extends RuntimableRuntime<ComponentProperties> {

    /**
     * Validate connection for given connection properties.
     *
     * @param properties connection properties
     * @return result of validation
     */
    ValidationResult validateConnection(GoogleDriveConnectionProperties properties);
}
