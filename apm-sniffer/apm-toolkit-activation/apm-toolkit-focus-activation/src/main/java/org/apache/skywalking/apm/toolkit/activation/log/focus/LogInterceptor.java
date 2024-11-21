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

package org.apache.skywalking.apm.toolkit.activation.log.focus;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.toolkit.logging.common.log.SkyWalkingContext;
import org.apache.skywalking.apm.util.StringUtil;
import com.sui.creep.collector.log.Log;
import java.lang.reflect.Method;

import static org.apache.skywalking.apm.toolkit.activation.log.focus.Constants.TID;

/**
 * for mysql connector java 6.0.4+
 */
public class LogInterceptor implements InstanceMethodsAroundInterceptor {

    private static final String EMPTY_TRACE_CONTEXT_ID = "N/A";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] parameterTypes,
        MethodInterceptResult result) {
        Log log = (Log) allArguments[0];
        String swTraceId = (String) log.getContext() .get(TID);
        if (!isUnset(swTraceId)) {
            return ;
        }
        if (!ContextManager.isActive() && allArguments[0] instanceof EnhancedInstance) {
            SkyWalkingContext skyWalkingContext = (SkyWalkingContext) ((EnhancedInstance) allArguments[0]).getSkyWalkingDynamicField();
            if (skyWalkingContext != null) {
                swTraceId =  skyWalkingContext.getTraceId();
            }
        }
        if (isUnset(swTraceId)) {
            swTraceId = ContextManager.getGlobalTraceId();
        }
        log.getContext().put(TID, swTraceId);
    }

    private boolean isUnset(String swTraceId) {
        return StringUtil.isBlank(swTraceId) || EMPTY_TRACE_CONTEXT_ID.equals(swTraceId);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] parameterTypes,
        Object ret) {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] parameterTypes,
        Throwable t) {

    }
}
