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

package org.apache.skywalking.apm.agent.core.context.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.skywalking.apm.agent.core.conf.Config;

public enum StatusChecker {

    /**
     * All exception is thought as error status.
     */
    OFF(Collections.singletonList(new OffExceptionCheckStrategy())),

    /**
     * Do hierarchy check for the exception. Tag span as error status unless it is listed in
     * Config.StatusCheck#IGNORED_EXCEPTIONS or tagged with @IgnoredException
     * @see org.apache.skywalking.apm.toolkit.trace.IgnoredException
     * @see Config.StatusCheck#IGNORED_EXCEPTIONS
     */
    HIERARCHY_MATCH(Arrays.asList(
        new HierarchyMatchExceptionCheckStrategy(),
        new AnnotationMatchExceptionCheckStrategy()
    ));

    private final List<ExceptionCheckStrategy> strategies;

    StatusChecker(final List<ExceptionCheckStrategy> strategies) {
        this.strategies = strategies;
    }

    public boolean checkStatus(Throwable e) {
        int maxDepth = Config.StatusCheck.MAX_RECURSIVE_DEPTH;
        boolean isError = true;
        while (isError && Objects.nonNull(e) && maxDepth-- != 0) {
            isError = check(e);
            e = e.getCause();
        }
        return isError;
    }

    private boolean check(final Throwable e) {
        return strategies.stream().allMatch(item -> item.isError(e));
    }
}
