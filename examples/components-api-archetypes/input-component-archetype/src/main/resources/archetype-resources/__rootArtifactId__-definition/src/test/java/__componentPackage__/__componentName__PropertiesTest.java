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
package ${package}.${componentPackage};

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import ${package}.StringDelimiter;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class ${componentName}PropertiesTest {

	/**
	 * Checks forms are filled with required widgets
	 */
	@Test
	public void testSetupLayout() {
		${componentName}Properties properties = new ${componentName}Properties("root");
		properties.schema.init();

		properties.setupLayout();

		Form main = properties.getForm(Form.MAIN);
		assertThat(main, notNullValue());

		Collection<Widget> mainWidgets = main.getWidgets();
		assertThat(mainWidgets, hasSize(6));

		Widget schemaWidget = main.getWidget("schema");
		assertThat(schemaWidget, notNullValue());

		Widget fileWidget = main.getWidget("filename");
		assertThat(fileWidget, notNullValue());

		Widget useCustomDelimiterWidget = main.getWidget("useCustomDelimiter");
		assertThat(useCustomDelimiterWidget, notNullValue());

		Widget delimiterWidget = main.getWidget("delimiter");
		assertThat(delimiterWidget, notNullValue());

		Widget customDelimiterWidget = main.getWidget("customDelimiter");
		assertThat(customDelimiterWidget, notNullValue());

		Widget guessSchemaWidget = main.getWidget("guessSchema");
		assertThat(guessSchemaWidget, notNullValue());
	}

	/**
	 * Checks default values are set correctly
	 */
	@Test
	public void testSetupProperties() {
		${componentName}Properties properties = new ${componentName}Properties("root");
		properties.setupProperties();

		StringDelimiter delimiter = properties.delimiter.getValue();
		assertThat(delimiter, equalTo(StringDelimiter.SEMICOLON));

		boolean useCustomDelimiter = properties.useCustomDelimiter.getValue();
		assertEquals(false, useCustomDelimiter);

		String customDelimiter = properties.customDelimiter.getValue();
		assertThat(customDelimiter, equalTo(""));
	}

	/**
	 * Checks initial layout
	 */
	@Test
	public void testRefreshLayout() {
		${componentName}Properties properties = new ${componentName}Properties("root");
		properties.init();

		properties.refreshLayout(properties.getForm(Form.MAIN));

		boolean schemaHidden = properties.getForm(Form.MAIN).getWidget("schema").isHidden();
		assertFalse(schemaHidden);

		boolean filenameHidden = properties.getForm(Form.MAIN).getWidget("filename").isHidden();
		assertFalse(filenameHidden);

		boolean useCustomDelimiterHidden = properties.getForm(Form.MAIN).getWidget("useCustomDelimiter").isHidden();
		assertFalse(useCustomDelimiterHidden);

		boolean delimiterHidden = properties.getForm(Form.MAIN).getWidget("delimiter").isHidden();
		assertFalse(delimiterHidden);

		boolean customDelimiterHidden = properties.getForm(Form.MAIN).getWidget("customDelimiter").isHidden();
		assertTrue(customDelimiterHidden);

		boolean guessSchemaHidden = properties.getForm(Form.MAIN).getWidget("guessSchema").isHidden();
		assertFalse(guessSchemaHidden);
	}
}
