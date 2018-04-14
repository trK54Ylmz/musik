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

import com.musik.config.Config;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class WebServer {
    private final Config config;

    private Server server;

    public WebServer(Config config) {
        this.config = config;
    }

    public void start() throws Exception {
        server = new Server(config.getPort());

        enableSslSupport();

        server.setHandler(getServletHandler(getContext()));

        server.start();
        server.join();
    }

    /**
     * Enable SSL (HTTPS) support
     *
     * Audio inputs require SSL support for remote servers
     */
    private void enableSslSupport() throws Exception {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(config.getPort());

        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        final String password = "123654";

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(getClass().getResourceAsStream("/server.jks"), password.toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(getClass().getResourceAsStream("/trust.jks"), password.toCharArray());

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyStorePassword(password);
        sslContextFactory.setKeyManagerPassword(password);
        sslContextFactory.setTrustStore(trustStore);
        sslContextFactory.setTrustStorePassword(password);
        sslContextFactory.setWantClientAuth(true);

        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        sslConnector.setPort(config.getPort() - 1);
        sslConnector.setIdleTimeout(150000);

        server.setConnectors(new Connector[]{connector, sslConnector});
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