/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musik.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.TestClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemPropertyRunner extends Runner {
    private List<Method> beforeClass = new ArrayList<>();

    private List<Method> afterClass = new ArrayList<>();

    private List<Method> tests = new ArrayList<>();

    private final TestClass tc;

    public SystemPropertyRunner(Class<?> cls) {
        this.tc = new TestClass(cls);

        boolean runExclusives = System.getProperty("exclusives") != null;

        List<Method> superClass = Arrays.asList(cls.getSuperclass().getDeclaredMethods());
        List<Method> thisClass = Arrays.asList(cls.getDeclaredMethods());

        Method[] methods = new Method[superClass.size() + thisClass.size()];

        methods = ImmutableList.copyOf(Iterables.concat(superClass, thisClass)).toArray(methods);

        for (int i = 0; i < methods.length; i++) {
            int modifier = methods[i].getModifiers();
            int count = methods[i].getParameterCount();
            Class<?> returnType = methods[i].getReturnType();

            if (returnType == null || count != 0 || !Modifier.isPublic(modifier)
                    || Modifier.isInterface(modifier) || Modifier.isAbstract(modifier)) {
                continue;
            }

            if (methods[i].getAnnotation(BeforeClass.class) != null) {
                beforeClass.add(methods[i]);
                continue;
            }


            if (methods[i].getAnnotation(AfterClass.class) != null) {
                afterClass.add(methods[i]);
                continue;
            }

            if (methods[i].getAnnotation(Ignore.class) == null) {
                tests.add(methods[i]);
            }

            // does not allow to run exclusive tests if exclusive property did not specify
            if (methods[i].getAnnotation(Exclusive.class) != null && !runExclusives) {
                tests.remove(methods[i]);
            }
        }
    }

    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(tc.getName(), tc.getJavaClass().getAnnotations());
    }

    /**
     * Invoke Java method
     *
     * @param method the public method of test class
     * @param cls    the test class
     */
    private void invoke(Method method, Class<?> cls)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        method.invoke(cls.getConstructors()[0].newInstance());
    }

    @Override
    public void run(RunNotifier notifier) {
        for (int i = 0; i < beforeClass.size(); i++) {
            try {
                invoke(beforeClass.get(i), tc.getJavaClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < tests.size(); i++) {
            try {
                Method method = tests.get(i);
                Description desc = Description.createTestDescription(method.getClass(), method.getName());

                notifier.fireTestStarted(desc);

                invoke(method, tc.getJavaClass());

                notifier.fireTestFinished(desc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < afterClass.size(); i++) {
            try {
                invoke(afterClass.get(i), tc.getJavaClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}