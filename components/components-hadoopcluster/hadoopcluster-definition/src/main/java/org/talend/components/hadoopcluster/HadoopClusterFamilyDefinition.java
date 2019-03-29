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

package org.talend.components.hadoopcluster;

import static org.talend.components.api.Constants.COMPONENT_INSTALLER_PREFIX;

import org.osgi.service.component.annotations.Component;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.hadoopcluster.configuration.input.HadoopClusterConfigurationInputDefinition;

import com.google.auto.service.AutoService;

@AutoService(ComponentInstaller.class)
@Component(name = COMPONENT_INSTALLER_PREFIX + HadoopClusterFamilyDefinition.NAME, service = ComponentInstaller.class)
public class HadoopClusterFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "HadoopCluster";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_RUNTIME_ARTIFACT_ID = "hadoopcluster-runtime";

    public static final String MAVEN_RUNTIME_URI = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_RUNTIME_ARTIFACT_ID;

    // TODO For TUP-17240, use one runtime module instead of independent runtime module
    // public static final String MAVEN_RUNTIME_ARTIFACT_ID_AMBARI = "hadoopcluster-runtime_ambari";
    //
    // public static final String MAVEN_RUNTIME_ARTIFACT_ID_CM = "hadoopcluster-runtime_cm";
    //
    // public static final String MAVEN_RUNTIME_URI_AMBARI = "mvn:" + MAVEN_GROUP_ID + "/" +
    // MAVEN_RUNTIME_ARTIFACT_ID_AMBARI;
    //
    // public static final String MAVEN_RUNTIME_URI_CM = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_RUNTIME_ARTIFACT_ID_CM;

    public HadoopClusterFamilyDefinition() {
        super(NAME, new HadoopClusterConfigurationInputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
