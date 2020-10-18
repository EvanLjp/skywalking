/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.agent.core.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.skywalking.apm.agent.core.boot.OverrideImplementor;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.util.CollectionUtil;
import org.apache.skywalking.apm.util.StringUtil;

import static org.apache.skywalking.apm.agent.core.context.ContextManagerExtendAdvancedConfig.Plugin.ExtendContextManager.EXTERNAL_INJECTION_KEY;
import static org.apache.skywalking.apm.agent.core.context.ContextManagerExtendAdvancedConfig.Plugin.ExtendContextManager.EXTERNAL_INJECTION_TOKEN;
import static org.apache.skywalking.apm.agent.core.context.ContextManagerExtendAdvancedConfig.Plugin.ExtendContextManager.EXTERNAL_INJECTION_SWITCH;
import static org.apache.skywalking.apm.agent.core.context.ContextManagerExtendAdvancedConfig.Plugin.ExtendContextManager.INJECTION_TAGS;

/**
 * The default correlationContextService is only propagate the {@link CorrelationContext}.
 */
@OverrideImplementor(ContextManagerExtendService.class)
public class ContextManagerExtendAdvancedService extends ContextManagerExtendService {

    private List<String> injectionTags = new ArrayList<>();

    /**
     * Init the advanced config.
     */
    @Override
    public void boot() {
        if (StringUtil.isNotEmpty(INJECTION_TAGS)) {
            injectionTags = Arrays.asList(INJECTION_TAGS.split(","));
        }
    }

    @Override
    public void extract(final ContextCarrier carrier, AbstractTracerContext tracingContext) {
        super.extract(carrier, tracingContext);
        if (EXTERNAL_INJECTION_SWITCH && !carrier.isValid()) {
            Boolean isLegalInjection = carrier.getCorrelationContext().get(EXTERNAL_INJECTION_KEY)
                                              .map(item -> EXTERNAL_INJECTION_TOKEN.equals(item))
                                              .orElse(false);
            if (isLegalInjection) {
                carrier.getCorrelationContext().remove(EXTERNAL_INJECTION_KEY);
                carrier.getCorrelationContext().extract(carrier);
            }
        }
    }

    @Override
    public void injectSpan(final AbstractSpan span, final CorrelationContext correlationContext) {
        if (!CollectionUtil.isEmpty(injectionTags)) {
            injectionTags.forEach(
                key -> correlationContext.get(key).ifPresent(val -> span.tag(new StringTag(key), val)));
        }
    }
}
