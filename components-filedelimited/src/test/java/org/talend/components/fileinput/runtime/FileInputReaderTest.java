package org.talend.components.fileinput.runtime;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;
import org.talend.components.fileinput.tFileInputDelimited.TFileInputDelimitedDefinition;
import org.talend.components.fileinput.tFileInputDelimited.TFileInputDelimitedProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
@WebIntegrationTest("server.port:0")
public class FileInputReaderTest {

	@Inject
	private ComponentService componentService;

	private FileInputReader reader;

	@Value("${local.server.port}")
	private int serverPort;

	@Before
	public void setReader() {
		TFileInputDelimitedDefinition definition = (TFileInputDelimitedDefinition) componentService
				.getComponentDefinition("TFileInputDelimited");
		TFileInputDelimitedProperties properties = (TFileInputDelimitedProperties) definition.createProperties();
		FileInputSource source = (FileInputSource) definition.getRuntime();
		source.initialize(null, properties);
		reader = (FileInputReader) source.createReader(null);
	}

	@Test
	public void testStart() throws Exception {
		Assert.assertTrue(reader.start());
		while (reader.advance()) {
			Assert.assertNotNull(reader.getCurrent());
		}
		reader.close();
	}
}