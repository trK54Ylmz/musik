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

package com.musik.config;

import com.musik.Utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;

public class ConfigFactory {
    private static final Logger LOGGER = Logger.getLogger(ConfigFactory.class);

    private static final DefaultParser PARSER = new DefaultParser();

    private static final DateTimeFormatter DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private ConfigFactory() {
        // ignore this block
    }

    /**
     * Casts the argument value to target type which is defined in configuration class
     *
     * @param value the argument value as String
     * @param type  the target value type
     * @return the target value
     */
    private static Object cast(String value, Class<?> type) {
        if (value == null) {
            return null;
        }

        if (type == String.class) {
            return value;
        } else if (type == Float.class || type == float.class) {
            return Float.parseFloat(value);
        } else if (type == Double.class || type == double.class) {
            return Float.parseFloat(value);
        } else if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == Short.class || type == short.class) {
            return Short.parseShort(value);
        } else if (type == Byte.class || type == byte.class) {
            return Byte.parseByte(value);
        } else if (type == Timestamp.class) {
            return new Timestamp(TIME.parseMillis(value));
        } else if (type == Date.class) {
            return new Date(DATE.parseMillis(value));
        } else if (type == java.util.Date.class) {
            throw new RuntimeException("Java date type is invalid. Please use java.sql.Date type");
        } else {
            throw new RuntimeException(Utils.s("Invalid type {0}", type.getName()));
        }
    }

    /**
     * Generates configuration object according to argument definition
     *
     * @param options  the argument definition
     * @param instance the instance of configuration class
     * @param <T>      the generic class of configuration class
     * @return the instance of the configuration class
     */
    private static <T> T fill(Option[] options, T instance)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class<?> tClass = instance.getClass();
        Field[] fields = tClass.getDeclaredFields();

        for (Field field : fields) {
            Argument argument = field.getAnnotation(Argument.class);

            if (argument == null) {
                continue;
            }

            for (Option option : options) {
                String name = argument.value();

                // choose field name if argument value is empty
                if ("".equals(name)) {
                    name = field.getName();
                }

                // skip the other fields that represents other types
                if (!option.getOpt().equals(name)) {
                    continue;
                }

                Method method = new PropertyDescriptor(field.getName(), tClass).getWriteMethod();
                method.invoke(instance, cast(option.getValue(), field.getType()));
            }
        }

        return instance;
    }

    /**
     * Generates argument definition by configuration class
     *
     * @param instance the instance of configuration class
     * @param <T>      the generic type of configuration class
     * @return the argument definition
     */
    private static <T> Options toOptions(T instance) {
        Options options = new Options();

        Field[] fields = instance.getClass().getDeclaredFields();

        for (Field field : fields) {
            Argument arg = field.getAnnotation(Argument.class);

            if (arg == null) {
                continue;
            }

            String name = arg.value();

            // choose field name if argument value is empty
            if ("".equals(name)) {
                name = field.getName();
            }

            options.addOption(new Option(name, arg.hasValue(), arg.description()));
        }

        return options;
    }

    /**
     * Generates configuration class according to command line arguments and configuration class
     *
     * @param args   the command line arguments
     * @param tClass the configuration class
     * @param <T>    the generic class of configuration class
     * @return the configuration class
     */
    public static <T> T load(String[] args, Class<T> tClass) {
        try {
            T instance = tClass.newInstance();

            CommandLine cli = PARSER.parse(toOptions(instance), args);

            return fill(cli.getOptions(), instance);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            throw new RuntimeException(e.getMessage(), e);
        }
    }
}