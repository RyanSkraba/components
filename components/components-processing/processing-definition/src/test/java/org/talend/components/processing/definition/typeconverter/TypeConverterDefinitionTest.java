package org.talend.components.processing.definition.typeconverter;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.runtime.RuntimeInfo;

import static org.hamcrest.Matchers.*;

public class TypeConverterDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ComponentDefinition def = new TypeConverterDefinition();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        Assert.assertThat(def.getName(), is("TypeConverter"));
        Assert.assertThat(def.getFamilies(), arrayContaining("Processing"));
        Assert.assertThat(def.getPropertiesClass(), is(equalTo((Object) TypeConverterProperties.class)));
        Assert.assertThat(def.getImagePath(DefinitionImageType.PALETTE_ICON_32X32), is("TypeConverter_icon32.png"));
        Assert.assertThat(def.getIconKey(), is("type-converter"));
        Assert.assertThat(def.getSupportedConnectorTopologies(), contains(ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.INCOMING_AND_OUTGOING);
        Assert.assertThat(runtimeInfo.getRuntimeClassName(),
                is("org.talend.components.processing.runtime.typeconverter.TypeConverterRuntime"));
        // The integration module tests things that aren't available in the RuntimeInfo module until after it is
        // installed in the local maven repository.
    }

    @Test
    public void testFailRuntimeInfoIncoming() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(ComponentsErrorCode.WRONG_CONNECTOR)));
        thrown.expectMessage("WRONG_CONNECTOR:{component=TypeConverter}");
        def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.INCOMING);
    }

    @Test
    public void testFailRuntimeInfoOutgoing() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(ComponentsErrorCode.WRONG_CONNECTOR)));
        thrown.expectMessage("WRONG_CONNECTOR:{component=TypeConverter}");
        def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.OUTGOING);
    }

    @Test
    public void testFailRuntimeInfoNone() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(ComponentsErrorCode.WRONG_CONNECTOR)));
        thrown.expectMessage("WRONG_CONNECTOR:{component=TypeConverter}");
        def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.NONE);
    }

    @Test
    public void testFailRuntimeInfoDi() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(ComponentsErrorCode.WRONG_EXECUTION_ENGINE)));
        thrown.expectMessage("WRONG_EXECUTION_ENGINE:{component=TypeConverter, requested=DI, available=[BEAM]}");
        def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING);
    }

    @Test
    public void testFailRuntimeInfoSparkBatch() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(ComponentsErrorCode.WRONG_EXECUTION_ENGINE)));
        thrown.expectMessage("WRONG_EXECUTION_ENGINE:{component=TypeConverter, requested=DI_SPARK_BATCH, available=[BEAM]}");
        def.getRuntimeInfo(ExecutionEngine.DI_SPARK_BATCH, null, ConnectorTopology.OUTGOING);
    }

    @Test
    public void testFailRuntimeInfoSparkStreaming() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(ComponentsErrorCode.WRONG_EXECUTION_ENGINE)));
        thrown.expectMessage("WRONG_EXECUTION_ENGINE:{component=TypeConverter, requested=DI_SPARK_STREAMING, available=[BEAM]}");
        def.getRuntimeInfo(ExecutionEngine.DI_SPARK_STREAMING, null, ConnectorTopology.OUTGOING);
    }

}
