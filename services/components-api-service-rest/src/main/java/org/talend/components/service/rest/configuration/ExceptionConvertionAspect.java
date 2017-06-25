// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.service.rest.configuration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

/**
 * this aspect is used to intercept all exceptions in a @RequesMapping endpoint in order to convert them to
 * TalendRuntimeException. We need this because spring store Exception classes into a cache and the Component rest api
 * sometime create a temporary classloader that is supposed to disappear at the end of the call. But because Spring
 * caches exceptions class and method handlers the classloader cannot be garbage collected along with all it's classes.
 */
@Configuration
@Aspect
@SuppressWarnings("InsufficientBranchCoverage")
class ExceptionConvertionAspect {

    // there is not way to create a point cut on all method implmenting an interface with a given annotation.
    // this is a horrible hack to list all interface to

    @Around("execution(public * org.talend.components.service.rest.ComponentController .*(..)) "
            + "|| execution(public * org.talend.components.service.rest.DefinitionsController .*(..)) "
            + "|| execution(public * org.talend.components.service.rest.PropertiesController .*(..)) "
            + "|| execution(public * org.talend.components.service.rest.RuntimesController .*(..)) ")
    // + "|| execution(public *
    // org.talend.components.service.rest.configuration.RestProcessingExceptionThrowingControllerI .*(..)) ") // test
    // class
    public Object exception(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (TalendRuntimeException e) {
            throw e; // Let TalendRuntimeException pass through (to be processed in correct HTTP code by controller
                     // advice).
        } catch (Exception e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}