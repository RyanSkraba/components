package org.talend.component;

public class ComponentConnector {
	
	public enum Type {FLOW, ITERATE, SUBJOB_OK, SUBJOB_ERROR, COMPONENT_OK, COMPONENT_ERROR, RUN_IF };
	
	protected Type type;
	protected int maxInput;
	protected int maxOutput;

	
	public ComponentConnector(Type type, int maxInput, int maxOutput) {
		this.type = type;
		this.maxInput = maxInput;
		this.maxOutput = maxOutput;
	}
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getMaxInput() {
		return maxInput;
	}
	public void setMaxInput(int maxInput) {
		this.maxInput = maxInput;
	}
	public int getMaxOutput() {
		return maxOutput;
	}
	public void setMaxOutput(int maxOutput) {
		this.maxOutput = maxOutput;
	}

}
