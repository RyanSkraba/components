package org.talend.components.fileinput.tFileOutputDelimited;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.fileinput.FileInputDefinition;
import org.talend.components.fileinput.runtime.FileOutputSink;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
		+ TFileOutputDelimitedDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TFileOutputDelimitedDefinition extends FileInputDefinition implements OutputComponentDefinition {

	public static final String COMPONENT_NAME = "tFileOutputDelimitedNew"; //$NON-NLS-1$

	public TFileOutputDelimitedDefinition() {
		super(COMPONENT_NAME);
	}

	@Override
	public Class<? extends ComponentProperties> getPropertyClass() {
		return TFileOutputDelimitedProperties.class;
	}

	@Override
	public Sink getRuntime() {
		// TODO Auto-generated method stub
		return new FileOutputSink();
	}

}
