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

package org.talend.components.bigquery;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;


public class BigQueryDatasetDefinition extends I18nDefinition implements DatasetDefinition<BigQueryDatasetProperties> {

    public static final String RUNTIME = "org.talend.components.bigquery.runtime.BigQueryDatasetRuntime";

    public static final String NAME = BigQueryComponentFamilyDefinition.NAME + "Dataset";

    public BigQueryDatasetDefinition() {
        super(NAME);
    }

    @Override
    public Class<BigQueryDatasetProperties> getPropertiesClass() {
        return BigQueryDatasetProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(BigQueryDatasetProperties properties) {
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/bigquery-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "bigquery-runtime"), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public String getImagePath(DefinitionImageType definitionImageType) {
        switch (definitionImageType) {
            case PALETTE_ICON_32X32:
                return NAME + "_icon32.png";
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return "bigquery";
    }

}
