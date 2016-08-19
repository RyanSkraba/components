package org.talend.components.fileinput.tFileInputDelimited;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.fileinput.FileInputProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TFileInputDelimitedProperties extends FileInputProperties {

	public TFileInputDelimitedProperties(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public enum CSVRowSeparator {
		LF, CR, CRLF
	}

	public Property<String> rowSeparator = PropertyFactory.newString("rowSeparator");
	public Property<String> fieldSeparator = PropertyFactory.newString("fieldSeparator");
	public Property<Boolean> csvOptions = PropertyFactory.newBoolean("csvOptions");
	public Property<String> escapeChar = PropertyFactory.newString("escapeChar");
	public Property<String> textEnclosure = PropertyFactory.newString("textEnclosure");
	public Property<Integer> head = PropertyFactory.newInteger("head");
	public Property<Integer> foot = PropertyFactory.newInteger("foot");
	public Property<Integer> limit = PropertyFactory.newInteger("limit");
	public Property<Boolean> removeEmptyRow = PropertyFactory.newBoolean("removeEmptyRow");
	public Property<Boolean> uncompress = PropertyFactory.newBoolean("uncompress");
	public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");

	// Advanced
	public enum EncodingType {
		ISO8859_15, UTF_8, CUSTOM
	}

	public Property<Boolean> advancedSeparator = PropertyFactory.newBoolean("advancedSeparator");
	public Property<String> thousandsSeparator = PropertyFactory.newString("thousandsSeparator");
	public Property<String> decimalSeparator = PropertyFactory.newString("decimalSeparator");
	public Property<Boolean> random = PropertyFactory.newBoolean("random");
	public Property<Integer> nbRandom = PropertyFactory.newInteger("nbRandom");
	public Property<EncodingType> encodingType = PropertyFactory.newEnum("encodingType", EncodingType.class);
	public Property<Boolean> trimall = PropertyFactory.newBoolean("trimall");
	public Property<Boolean> checkFieldsNum = PropertyFactory.newBoolean("checkFieldsNum");
	public Property<Boolean> checkDate = PropertyFactory.newBoolean("checkDate");
	public Property<Boolean> splitRecord = PropertyFactory.newBoolean("splitRecord");
	public Property<Boolean> enableDecode = PropertyFactory.newBoolean("enableDecode");
	public Property<Boolean> tstatCatcherStat = PropertyFactory.newBoolean("tstatCatcherStat");

	@Override
	public void setupProperties() {
		super.setupProperties();
		rowSeparator.setValue("\\n");
		fieldSeparator.setValue(";");
		escapeChar.setValue("\"\"");
		textEnclosure.setValue("\"\"");
		head.setValue(0);
		foot.setValue(0);
		thousandsSeparator.setValue(",");
		decimalSeparator.setValue(".");
		nbRandom.setValue(10);
	}

	@Override
	public void setupLayout() {
		super.setupLayout();
		Form form = getForm(Form.MAIN);

		form.addRow(rowSeparator);
		form.addRow(fieldSeparator);
		form.addRow(csvOptions);
		form.addRow(escapeChar);
		form.addRow(textEnclosure);
		form.addRow(random);
		form.addRow(nbRandom);
		form.addRow(head);
		form.addRow(foot);
		form.addRow(limit);
		form.addRow(removeEmptyRow);
		form.addRow(uncompress);
		form.addRow(dieOnError);

		Form advancedForm = Form.create(this, Form.ADVANCED);
		advancedForm.addRow(advancedSeparator);
		advancedForm.addRow(thousandsSeparator);
		advancedForm.addRow(decimalSeparator);
		advancedForm.addRow(encodingType);
		advancedForm.addRow(trimall);
		advancedForm.addRow(checkFieldsNum);
		advancedForm.addRow(checkDate);
		advancedForm.addRow(splitRecord);
		advancedForm.addRow(enableDecode);

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
			form.getWidget(foot.getName()).setHidden(uncompress.getValue());
		}
		if (form.getName().equals(Form.ADVANCED)) {
			form.getWidget(thousandsSeparator.getName()).setHidden(!advancedSeparator.getValue());
			form.getWidget(decimalSeparator.getName()).setHidden(!advancedSeparator.getValue());
			form.getWidget(rowSeparator.getName()).setHidden(csvOptions.getValue());
			form.getWidget(random.getName()).setHidden(csvOptions.getValue());
			form.getWidget(nbRandom.getName()).setHidden(csvOptions.getValue() || !random.getValue());
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
