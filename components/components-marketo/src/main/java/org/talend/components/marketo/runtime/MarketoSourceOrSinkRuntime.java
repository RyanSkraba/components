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
package org.talend.components.marketo.runtime;

import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.daikon.properties.ValidationResult;

public interface MarketoSourceOrSinkRuntime extends SourceOrSink {

    ValidationResult validateConnection(MarketoProvideConnectionProperties properties);

}
