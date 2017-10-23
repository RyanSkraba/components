package org.talend.components.service.rest.configuration;

import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME;

import java.util.IllformedLocaleException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Resolve the locale from the web Query. If {@link #LANGUAGE_QUERY_PARAMETER_NAME language} parameter is present, parse
 * it as IETF BCP 47 language tag string, else, fallback to JVM default through {@link Locale#getDefault()}.
 * 
 * @see Locale#forLanguageTag(String)
 * @see Locale#getDefault()
 */
@Component(LOCALE_RESOLVER_BEAN_NAME)
public class RequestParameterLocaleResolver implements LocaleResolver {

    /**
     * Name of the parameter where the locale might be defined.
     */
    public static final String LANGUAGE_QUERY_PARAMETER_NAME = "language";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale defaultLocale = Locale.getDefault();
        String language = request.getParameter(LANGUAGE_QUERY_PARAMETER_NAME);
        if (defaultLocale != null && language == null) {
            return defaultLocale;
        }

        Locale resolvedLocale;
        try {
            resolvedLocale = new Locale.Builder().setLanguageTag(language).build();
        } catch (IllformedLocaleException e) {
            resolvedLocale = Locale.getDefault();
        }
        return resolvedLocale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("Cannot change HTTP accept header - use a different locale resolution strategy");
    }
}
