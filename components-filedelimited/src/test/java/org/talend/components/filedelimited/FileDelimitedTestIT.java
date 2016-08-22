package org.talend.components.filedelimited;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tFileOutputDelimited.TFileOutputDelimitedDefinition;
import org.talend.daikon.properties.presentation.Form;

import static org.junit.Assert.assertEquals;

/**
 * Created by Talend on 2016-08-22.
 */
public class FileDelimitedTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedTestIT.class);

    public FileDelimitedTestIT() {
        super();
    }

    @Test
    public void testGetProps() {
        // Input delimited
        ComponentProperties input = new TFileInputDelimitedDefinition().createProperties();
        Form inputForm = input.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(input, errorCollector);
        LOGGER.debug(inputForm.toString());
        LOGGER.debug(input.toString());
        assertEquals(Form.MAIN, inputForm.getName());

        // Output delimited
        ComponentProperties output = new TFileOutputDelimitedDefinition().createProperties();
        Form outputForm = output.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(output, errorCollector);
        LOGGER.debug(outputForm.toString());
        LOGGER.debug(output.toString());
        assertEquals(Form.MAIN, outputForm.getName());
    }
}
