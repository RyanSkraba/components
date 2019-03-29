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
package org.talend.components.marketo.runtime;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

public class DisableIfMissingConfig implements TestRule {
    private final String systemPropName;

    public DisableIfMissingConfig(final String systemPropName) {
        this.systemPropName = systemPropName;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (System.getProperty(systemPropName, "").trim().isEmpty()) {
            return new Statement() {
                @Override
                public void evaluate() {
                    LoggerFactory.getLogger(DisableIfMissingConfig.class)
                                 .warn("Missing system property '{}', skipping {}", systemPropName, description);
                }
            };
        }
        return base;
    }
}
