
package org.talend.components.multiruntime;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.multiruntime.MultiRuntimeComponentProperties.Version;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;

/**
 * The MultiRuntimeComponentDefinition acts as an entry point for all of services that
 * a component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
public class MultiRuntimeComponentDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "MultiRuntimeComponent"; //$NON-NLS-1$

    public MultiRuntimeComponentDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Input" }; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        default:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        }
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return MultiRuntimeComponentProperties.class;
    }

    public static RuntimeInfo getRuntimeInfoFromVersion(Version version) {
        RuntimeInfo result = null;
        switch (version) {
        case VERSION_0_1:
            result = new RuntimeInfo() {

                @Override
                public String getRuntimeClassName() {
                    return "org.talend.multiruntime.MultiRuntimeComponentSource";
                }

                @Override
                public List<URL> getMavenUrlDependencies() {
                    try {
                        return new ArrayList<>(Arrays.asList(
                                new URL("mvn:org.talend.components/test-multiple-runtime-comp-runtime-v01/0.1.0-SNAPSHOT"),
                                new URL("mvn:org.talend.test/zeLib/0.0.1-SNAPSHOT")));
                    } catch (MalformedURLException e) {
                        throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION);
                    }
                }
            };
            break;
        case VERSION_0_2:
            result = new RuntimeInfo() {

                @Override
                public String getRuntimeClassName() {
                    return "org.talend.multiruntime.MultiRuntimeComponentSource";
                }

                @Override
                public List<URL> getMavenUrlDependencies() {
                    try {
                        return new ArrayList<>(Arrays.asList(
                                new URL("mvn:org.talend.components/test-multiple-runtime-comp-runtime-v02/0.1.0-SNAPSHOT"),
                                new URL("mvn:org.talend.test/zeLibV2/0.0.2-SNAPSHOT")));
                    } catch (MalformedURLException e) {
                        throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION);
                    }

                }
            };
            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties prop, ConnectorTopology connectorTopology) {
        if (connectorTopology == ConnectorTopology.OUTGOING) {
            if (prop != null && prop instanceof MultiRuntimeComponentProperties) {
                MultiRuntimeComponentProperties mrcpProp = (MultiRuntimeComponentProperties) prop;
                return getRuntimeInfoFromVersion(mrcpProp.version.getValue());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

}
