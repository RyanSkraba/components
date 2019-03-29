/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.components.test;

import org.apache.commons.lang3.SystemUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

public class DisableAfterJava8 implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        if (!SystemUtils.JAVA_VERSION.startsWith("1.8.")) {
            return new Statement() {
                @Override
                public void evaluate() {
                    LoggerFactory.getLogger(DisableAfterJava8.class)
                                 .warn("Java {} not supported yet, skipping {}", SystemUtils.JAVA_VERSION, description);
                }
            };
        }
        return base;
    }
}
