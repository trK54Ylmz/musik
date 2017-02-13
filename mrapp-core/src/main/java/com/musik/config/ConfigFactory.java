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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFactory {
    private String path;

    private ConfigFactory() {
    }

    public ConfigFactory set(Configs sys) {
        if (Strings.isNullOrEmpty(System.getProperty(sys.getPath()))) {
            throw new NullPointerException(sys.getPath() + " is empty");
        }

        this.path = System.getProperty(sys.getPath());

        return this;
    }

    public Properties load() {
        Preconditions.checkNotNull(path, "Please use set method to choose path of config file");

        try (InputStream stream = new FileInputStream(path)) {
            final Properties properties = new Properties();
            properties.load(stream);

            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConfigFactory build() {
        return new ConfigFactory();
    }
}