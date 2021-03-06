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

package com.musik;

import com.musik.config.Config;
import com.musik.config.ConfigFactory;
import com.musik.server.WebServer;

import org.apache.log4j.Logger;

public class WebApp {
    private static final Logger LOGGER = Logger.getLogger(WebApp.class);

    public static void main(String[] args) {
        final Config config = ConfigFactory.load(args, Config.class);

        final WebServer server = new WebServer(config);

        // wait for kill signal
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            LOGGER.info("Web server starting ...");

            server.start();
        } catch (Throwable t) {
            LOGGER.fatal(t.getMessage(), t);
        }
    }
}