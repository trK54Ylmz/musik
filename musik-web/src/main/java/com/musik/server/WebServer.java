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

package com.musik.server;

import com.musik.config.ConfigFactory;
import com.musik.config.Configs;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.util.Properties;

public class WebServer {
    private static final Properties CONFIG = ConfigFactory.build().set(Configs.WEB).load();

    private Server server;

    public void start() throws Exception {
        final int port = Integer.parseInt(CONFIG.getProperty("port"));

        server = new Server(port);
        server.setHandler(getServletHandler(getContext()));

        server.start();
        server.join();
    }

    /**
     * Returns configured servlet handler named default
     *
     * @param context the application context
     * @return the servlet handler
     */
    private static ServletContextHandler getServletHandler(WebApplicationContext context)
            throws IOException {
        ServletContextHandler handler = new ServletContextHandler();
        handler.setErrorHandler(null);
        handler.setContextPath("/");
        handler.setResourceBase(new ClassPathResource("webapp").getURI().toString());
        handler.addServlet(new ServletHolder("default", new DispatcherServlet(context)), "/*");
        handler.addEventListener(new ContextLoaderListener(context));

        return handler;
    }

    /**
     * Implements configuration that prepared by annotation based configuration classes
     *
     * @return the application context
     */
    private static WebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.setConfigLocations("com.musik.web");

        return ctx;
    }

    public void stop() {
        if (server == null || !server.isRunning()) {
            return;
        }

        try {
            server.stop();
        } catch (Exception e) {
            // ignore this block
        }
    }
}