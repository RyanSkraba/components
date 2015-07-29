package org.talend.component;

/**
 * Component design service.
 * 
 * An instance of this class handles the setup of the properties associated with
 * a component.
 * 
 * @author Francis Upton
 *
 */

public abstract class ComponentDesign {

	public enum Family {
		BUSINESS, CLOUD
	};

	public abstract ComponentProperties createProperties();

	public void setDesignerFamily(Family family) {

	}

	public abstract Family[] getSupportedFamilies();

}
