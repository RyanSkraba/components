package org.talend.components.filedelimited.wizard;

import java.io.IOException;
import java.util.Arrays;

import org.talend.components.filedelimited.runtime.FileDelimitedSource;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.service.Repository;

import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_CUSTOM;
import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_ISO_8859_15;
import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_UTF_8;
import static org.talend.daikon.properties.presentation.Widget.widget;

public class FileDelimitedWizardProperties extends TFileInputDelimitedProperties {

    private String repositoryLocation;

    public FileDelimitedWizardProperties(String name) {
        super(name);
    }

    public Property<String> name = PropertyFactory.newString("name").setRequired();

    public PresentationItem preview = new PresentationItem("preview", "Preview");

    // TODO check "Format"

    @Override
    public void setupProperties() {
        super.setupProperties();
        encoding.encodingType.setPossibleValues(Arrays.asList(ENCODING_TYPE_UTF_8, "UTF-16", "UTF-16LE", "UTF-16BE", "UTF-7",
                "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8",
                "ISO-8859-9", "ISO-8859-10", ENCODING_TYPE_ISO_8859_15, "windows-1252", "BIG5", "GB18030", "GB2312", "EUC_CN",
                ENCODING_TYPE_CUSTOM));
        encoding.encodingType.setValue(ENCODING_TYPE_UTF_8);
    }

    @Override
    public void setupLayout() {

        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(widget(fileName).setWidgetType(Widget.FILE_WIDGET_TYPE));
        wizardForm.addRow(encoding.getForm(Form.MAIN));
        wizardForm.addRow(rowSeparator);
        wizardForm.addColumn(fieldSeparator);
        wizardForm.addRow(csvOptions);
        wizardForm.addRow(escapeChar);
        wizardForm.addColumn(textEnclosure);
        wizardForm.addRow(header);
        wizardForm.addColumn(footer);
        wizardForm.addRow(removeEmptyRow);
        wizardForm.addRow(limit);
        wizardForm.addRow(widget(preview).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

    }

    public ValidationResult validateName() throws Exception {
        if (name.getValue() != null) {
            getForm(FORM_WIZARD).setAllowFinish(true);
        } else {
            getForm(FORM_WIZARD).setAllowFinish(false);
        }
        return ValidationResult.OK;
    }

    public ValidationResult afterFormFinishWizard(Repository<Properties> repo) throws Exception {
        // String connRepLocation =
        // TODO change the empty schema
        repo.storeProperties(this, this.name.getValue(), repositoryLocation, "main.schema");
        // repo.storeProperties(modProps, main.schema.getName(), connRepLocation, "main.schema");
        return ValidationResult.OK;
    }

    public FileDelimitedWizardProperties setRepositoryLocation(String location) {
        repositoryLocation = location;
        return this;
    }

    public ValidationResult afterPreview() {
        FileDelimitedSource source = new FileDelimitedSource();
        try {
            String jsonData = FileDelimitedSource.previewData(null, this, 200);
            //TODO show in wizard
        } catch (IOException e) {
            ValidationResult vr = new ValidationResult();
            vr.setMessage(e.getMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        return ValidationResult.OK;
    }

}
