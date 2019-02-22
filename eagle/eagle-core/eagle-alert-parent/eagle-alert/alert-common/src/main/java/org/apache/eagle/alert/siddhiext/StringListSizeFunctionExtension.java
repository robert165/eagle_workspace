/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eagle.alert.siddhiext;

import org.apache.commons.collections.ListUtils;
import org.apache.eagle.alert.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.List;

public class StringListSizeFunctionExtension extends FunctionExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(StringListSizeFunctionExtension.class);

    /**
     * The initialization method for StringListSizeFunctionExtension, this method will be called before the other methods.
     *
     * @param attributeExpressionExecutors the executors of each function parameter
     * @param executionPlanContext         the context of the execution plan
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to string:listSize() function, "
                    + "required 1, but found " + attributeExpressionExecutors.length);
        }

        Attribute.Type attributeType = attributeExpressionExecutors[0].getReturnType();
        if (attributeType != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the argument of string:listSize() function, "
                    + "required " + Attribute.Type.STRING
                    + ", but found " + attributeType.toString());
        }
    }

    /**
     * The main execution method which will be called upon event arrival.
     * when there are more than one function parameter
     * This method calculates subtraction of two List Of Strings
     * Each String is a jobs string needs to be loaded
     * @param data the runtime values of function parameters
     * @return the function result
     */
    @Override
    protected Object execute(Object[] data) {
        return null;
    }

    @Override
    protected Object execute(Object data) {
        try {
            return JsonUtils.jsonStringToList((String)data).size();
        } catch (Exception e) {
            LOG.warn("exception found {}", e);
            return 0;
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.INT;
    }

    @Override
    public Object[] currentState() {
        return null;
    }

    @Override
    public void restoreState(Object[] state) {
    }
}
