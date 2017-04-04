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
package org.talend.components.api.properties;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.properties.ComponentReferenceProperties.ReferenceType;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.serialize.SerializerDeserializer.Deserialized;

public class ComponentReferencePropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testComponentReferenceProperties() {
        // basic element
        ComponentReferenceProperties componentReferenceProperties = new ComponentReferenceProperties("testReference", "foo");
        assertEquals("testReference", componentReferenceProperties.getName());
        assertEquals(0, componentReferenceProperties.getForms().size());

        // init
        componentReferenceProperties.init();
    }

    @Test
    public void testReferenceType() {
        assertEquals(3, ReferenceType.values().length);
        assertArrayEquals(new ReferenceType[] { ReferenceType.THIS_COMPONENT, ReferenceType.COMPONENT_TYPE,
                ReferenceType.COMPONENT_INSTANCE }, ReferenceType.values());
    }

    // @Test
    // public void testEnclosingProps() {
    // TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("props").init();
    // System.out.println(props);
    // assertEquals(3, props.referencedComponent.getForms().size());
    // }

    @Test
    public void test() {
        PropWithReference fep = (PropWithReference) new PropWithReference("root").init();
        fep.strprop.setValue("val1");
        fep.referencedComponent.getReference().strprop2.setValue("val2");
        fep.referencedComponent.componentInstanceId.setValue("12345");
        fep.referencedComponent.referenceType.setValue(ReferenceType.COMPONENT_INSTANCE);
        assertEquals("val1", fep.strprop.getValue());
        assertEquals("val2", fep.referencedComponent.getReference().strprop2.getValue());
        assertEquals("12345", fep.referencedComponent.componentInstanceId.getValue());
        assertEquals(ReferenceType.COMPONENT_INSTANCE, fep.referencedComponent.referenceType.getValue());
        String serialized = fep.toSerialized();
        System.out.println(serialized);
        Deserialized<PropWithReference> fromSerializedPersistent = Properties.Helper.fromSerializedPersistent(serialized,
                PropWithReference.class);
        PropWithReference unserializedFep = fromSerializedPersistent.object;
        assertEquals("val1", unserializedFep.strprop.getValue());
        assertEquals("val2", unserializedFep.referencedComponent.getReference().strprop2.getValue());
        assertEquals("12345", unserializedFep.referencedComponent.componentInstanceId.getValue());
        assertEquals(ReferenceType.COMPONENT_INSTANCE, fep.referencedComponent.referenceType.getValue());

    }

    @Test
    public void testUnserializeOldReferenceProps() {
        String oldPropsStr = "{\"@id\":1,\"@type\":\"org.talend.components.api.properties.ComponentReferencePropertiesTest$PropWithReference\",\"strprop\":{\"@id\":4,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":\"val1\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"strprop\",\"displayName\":null,\"title\":null},\"referencedComponent\":{\"@id\":3,\"referenceType\":{\"@type\":\"org.talend.daikon.properties.property.EnumProperty\",\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":{\"@type\":\"org.talend.components.api.properties.ComponentReferenceProperties$ReferenceType\",\"name\":\"COMPONENT_INSTANCE\",\"ordinal\":2},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":{\"@type\":\"java.util.Arrays$ArrayList\",\"@items\":[{\"@type\":\"org.talend.components.api.properties.ComponentReferenceProperties$ReferenceType\",\"name\":\"THIS_COMPONENT\",\"ordinal\":0},{\"@type\":\"org.talend.components.api.properties.ComponentReferenceProperties$ReferenceType\",\"name\":\"COMPONENT_TYPE\",\"ordinal\":1},{\"@type\":\"org.talend.components.api.properties.ComponentReferenceProperties$ReferenceType\",\"name\":\"COMPONENT_INSTANCE\",\"ordinal\":2}]},\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"org.talend.components.api.properties.ComponentReferenceProperties.ReferenceType\",\"name\":\"referenceType\",\"displayName\":null,\"title\":null},\"componentType\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"DESIGN_TIME_ONLY\",\"ordinal\":2}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":\"AnotherPropDef\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"componentType\",\"displayName\":null,\"title\":null},\"componentInstanceId\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":\"12345\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"componentInstanceId\",\"displayName\":null,\"title\":null},\"componentProperties\":{\"@type\":\"org.talend.components.api.properties.ComponentReferencePropertiesTest$AnotherProperties\",\"strprop2\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":\"val2\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"strprop2\",\"displayName\":null,\"title\":null},\"name\":\"componentProperties\",\"validationResult\":null},\"enclosingProperties\":{\"@ref\":1},\"name\":\"referencedComponent\",\"validationResult\":null},\"name\":\"root\",\"validationResult\":null}\n";
        Deserialized<PropWithReference> fromSerializedPersistent = Properties.Helper.fromSerializedPersistent(oldPropsStr,
                PropWithReference.class);
        PropWithReference unserializedFep = fromSerializedPersistent.object;
        assertEquals("val1", unserializedFep.strprop.getValue());
        assertEquals("val2", unserializedFep.referencedComponent.getReference().strprop2.getValue());
        assertEquals("12345", unserializedFep.referencedComponent.componentInstanceId.getValue());
        assertEquals(ReferenceType.COMPONENT_INSTANCE, unserializedFep.referencedComponent.referenceType.getValue());
    }

    public static class PropWithReference extends ComponentPropertiesImpl {

        Property<String> strprop = PropertyFactory.newString("strprop");

        public final ComponentReferenceProperties<AnotherProperties> referencedComponent = new ComponentReferenceProperties<>("referencedComponent",
                "AnotherPropDef");

        public PropWithReference(String name) {
            super(name);
        }

        @Override
        public void setupProperties() {
            super.setupProperties();
            referencedComponent.setReference(new AnotherProperties("componentProperties").init());
        }

        @Override
        public void setupLayout() {
            super.setupLayout();
            Form mainForm = new Form(this, Form.MAIN);
            mainForm.addRow(strprop);

            // A form for a reference to a connection, used in a tSalesforceInput for example
            Form refForm = Form.create(this, Form.REFERENCE);
            Widget refWidget = Widget.widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
            refForm.addRow(refWidget);
            refForm.addRow(mainForm);

        }

    }

    public static class AnotherProperties extends ComponentPropertiesImpl {

        public final Property<String> strprop2 = PropertyFactory.newString("strprop2");

        public AnotherProperties(String name) {
            super(name);
        }

    }

}
