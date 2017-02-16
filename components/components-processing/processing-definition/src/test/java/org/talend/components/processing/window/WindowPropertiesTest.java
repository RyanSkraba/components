package org.talend.components.processing.window;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WindowPropertiesTest {

    /**
     * Checks {@link WindowProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        WindowProperties properties = new WindowProperties("test");
        assertThat(properties.windowLength.getValue(), notNullValue());
        assertThat(properties.windowSlideLength.getValue(), notNullValue());
        assertFalse(properties.windowSession.getValue());
    }

    /**
     * Checks {@link WindowProperties} sets correctly initial layout properties
     */
    @Test
    public void testSetupLayout() {
        WindowProperties properties = new WindowProperties("test");

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(3));
        Widget windowDuration = main.getWidget("windowLength");
        assertThat(windowDuration, notNullValue());
        Widget slideWindow = main.getWidget("windowSlideLength");
        assertThat(slideWindow, notNullValue());
        Widget sessionWindow = main.getWidget("windowSession");
        assertThat(sessionWindow, notNullValue());
    }

    /**
     * Checks {@link WindowProperties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayout() {
        WindowProperties properties = new WindowProperties("test");
        properties.init();

        properties.windowSession.setValue(false);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertTrue(properties.getForm(Form.MAIN).getWidget("windowLength").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("windowSlideLength").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("windowSession").isVisible());

        properties.windowSession.setValue(true);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertTrue(properties.getForm(Form.MAIN).getWidget("windowLength").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("windowSlideLength").isHidden());
        assertTrue(properties.getForm(Form.MAIN).getWidget("windowSession").isVisible());
    }
}
