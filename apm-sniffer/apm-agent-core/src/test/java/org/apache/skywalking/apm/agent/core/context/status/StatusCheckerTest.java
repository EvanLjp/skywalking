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

import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.skywalking.apm.agent.core.context.status.StatusChecker.HIERARCHY_MATCH;
import static org.apache.skywalking.apm.agent.core.context.status.StatusChecker.OFF;

public class StatusCheckerTest {

    @Before
    public void prepare() {
        Config.StatusCheck.IGNORED_EXCEPTIONS = "org.apache.skywalking.apm.agent.core.context.status.TestNamedMatchException";
        Config.StatusCheck.MAX_RECURSIVE_DEPTH = 1;
        ServiceManager.INSTANCE.boot();
    }

    @Test
    public void checkOffStatusChecker() {
        Assert.assertTrue(OFF.checkStatus(new Throwable()));
        Assert.assertTrue(OFF.checkStatus(new TestInheriteMatchException()));
        Assert.assertTrue(OFF.checkStatus(new TestNamedMatchException()));
        Assert.assertTrue(OFF.checkStatus(new IllegalArgumentException()));
    }

    @Test
    public void checkInheritNamedAndAnnotationMatchStatusChecker() {
        Assert.assertTrue(HIERARCHY_MATCH.checkStatus(new Throwable()));
        Assert.assertTrue(HIERARCHY_MATCH.checkStatus(new IllegalArgumentException()));
        Assert.assertFalse(HIERARCHY_MATCH.checkStatus(new TestNamedMatchException()));
        Assert.assertFalse(HIERARCHY_MATCH.checkStatus(new TestInheriteMatchException()));
        Assert.assertFalse(HIERARCHY_MATCH.checkStatus(new TestAnnotationMatchException()));
    }

}