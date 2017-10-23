package org.talend.components.service.rest.configuration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.talend.components.service.rest.configuration.RequestParameterLocaleResolver.LANGUAGE_QUERY_PARAMETER_NAME;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestParameterLocaleResolverTest {

    private RequestParameterLocaleResolver requestParameterLocaleResolver = new RequestParameterLocaleResolver();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    public void resolveLocale_shouldReadWellFormattedQueryParam() throws Exception {
        when(request.getParameter(eq(LANGUAGE_QUERY_PARAMETER_NAME)))
                .thenReturn(Locale.CHINA.toLanguageTag());

        Locale locale = requestParameterLocaleResolver.resolveLocale(request);

        assertEquals(Locale.CHINA, locale);
    }


    @Test
    public void resolveLocale_shouldFallbackToDefaultOnMalformed() throws Exception {
        // given
        Locale.setDefault(Locale.US);
        when(request.getParameter(eq(LANGUAGE_QUERY_PARAMETER_NAME))).thenReturn("David Bowie");

        // when
        Locale locale = requestParameterLocaleResolver.resolveLocale(request);

        // then
        assertEquals(Locale.US, locale);

        // and given
        Locale.setDefault(Locale.CHINA);

        // when
        locale = requestParameterLocaleResolver.resolveLocale(request);

        // then
        assertEquals(Locale.CHINA, locale);
    }

    @Test
    public void resolveLocale_shouldFallbackOnAbsentParam() throws Exception {
        // given
        Locale.setDefault(Locale.US);
        when(request.getParameter(eq(LANGUAGE_QUERY_PARAMETER_NAME))).thenReturn(null);

        // when
        Locale locale = requestParameterLocaleResolver.resolveLocale(request);

        // then
        assertEquals(Locale.US, locale);

        // and given
        Locale.setDefault(Locale.CHINA);

        // when
        locale = requestParameterLocaleResolver.resolveLocale(request);

        // then
        assertEquals(Locale.CHINA, locale);
    }
    @Test(expected = UnsupportedOperationException.class)
    public void setLocale_shouldThrowException() throws Exception {
        requestParameterLocaleResolver.setLocale(request, response, Locale.getDefault());
    }

}