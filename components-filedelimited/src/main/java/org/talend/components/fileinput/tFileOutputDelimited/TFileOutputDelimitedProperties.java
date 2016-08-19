package org.talend.components.fileinput.tFileOutputDelimited;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.fileinput.FileInputProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TFileOutputDelimitedProperties extends FileInputProperties {

	public TFileOutputDelimitedProperties(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public Property<Boolean> useStream = PropertyFactory.newBoolean("useStream");
	public Property<String> rowSeparator = PropertyFactory.newString("rowSeparator");
	public Property<String> fieldSeparator = PropertyFactory.newString("fieldSeparator");
	public Property<String> textEnclosure = PropertyFactory.newString("textEnclosure");
	public Property<Boolean> csvOptions = PropertyFactory.newBoolean("csvOptions");
	public Property<String> escapeChar = PropertyFactory.newString("escapeChar");
	public Property<Boolean> compress = PropertyFactory.newBoolean("compress");
	public Property<Boolean> append = PropertyFactory.newBoolean("append");
	public Property<Boolean> includeHead = PropertyFactory.newBoolean("includeHead");

	// Advanced
	public enum EncodingType {
		ISO8859_15, UTF_8, CUSTOM
	}

	public Property<Boolean> advancedSeparator = PropertyFactory.newBoolean("advancedSeparator");
	public Property<String> thousandsSeparator = PropertyFactory.newString("thousandsSeparator");
	public Property<String> decimalSeparator = PropertyFactory.newString("decimalSeparator");
	public Property<Boolean> creat = PropertyFactory.newBoolean("creat");
	public Property<Integer> split = PropertyFactory.newInteger("split");
	public Property<Integer> splitEvery = PropertyFactory.newInteger("splitEvery");
	public Property<Boolean> flushOnRow = PropertyFactory.newBoolean("flushOnRow");
	public Property<Integer> flushOnRowNum = PropertyFactory.newInteger("flushOnRowNum");
	public Property<Boolean> rowMode = PropertyFactory.newBoolean("rowMode");
	public Property<EncodingType> encodingType = PropertyFactory.newEnum("encodingType", EncodingType.class);
	public Property<Boolean> deleteEmptyFile = PropertyFactory.newBoolean("deleteEmptyFile");
	public Property<Boolean> tstatCatcherStat = PropertyFactory.newBoolean("tstatCatcherStat");

	@Override
	public void setupProperties() {
		super.setupProperties();
		rowSeparator.setValue("\\n");
		fieldSeparator.setValue(";");
		escapeChar.setValue("\"");
		thousandsSeparator.setValue(",");
		decimalSeparator.setValue(".");

	}

	@Override
	public void setupLayout() {
		super.setupLayout();
		Form form = getForm(Form.MAIN);
		form.addRow(schema.getForm(Form.REFERENCE));
		form.addRow(widget(filename).setWidgetType(Widget.FILE_WIDGET_TYPE));
		form.addRow(rowSeparator);
		form.addRow(escapeChar);
		form.addRow(fieldSeparator);
		form.addRow(csvOptions);
		form.addRow(compress);

		Form advancedForm = Form.create(this, Form.ADVANCED);
		advancedForm.addRow(advancedSeparator);
		advancedForm.addRow(thousandsSeparator);
		advancedForm.addRow(decimalSeparator);
		advancedForm.addRow(encodingType);
	}

	public void afterUncompress() {
		refreshLayout(getForm(Form.MAIN));
	}

	public void afterCsvOptions() {
		refreshLayout(getForm(Form.MAIN));
	}

	public void afterAdvancedSeparator() {
		refreshLayout(getForm(Form.ADVANCED));
	}

	public void afterRandom() {
		refreshLayout(getForm(Form.ADVANCED));
	}

	@Override
	public void refreshLayout(Form form) {
		super.refreshLayout(form);
		if (form.getName().equals(Form.MAIN)) {
			form.getWidget(escapeChar.getName()).setHidden(!csvOptions.getValue());
			form.getWidget(rowSeparator.getName()).setHidden(csvOptions.getValue());
		}
		if (form.getName().equals(Form.ADVANCED)) {
			form.getWidget(thousandsSeparator.getName()).setHidden(advancedSeparator.getValue());
			form.getWidget(decimalSeparator.getName()).setHidden(advancedSeparator.getValue());

		}
	}

	@Override
	protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
		if (isOutputConnection) {
			return Collections.singleton(MAIN_CONNECTOR);
		} else {
			return Collections.EMPTY_SET;
		}
	}
}
