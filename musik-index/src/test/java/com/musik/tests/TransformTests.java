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

import com.google.common.base.Preconditions;

import com.musik.index.ComplexNumber;
import com.musik.index.Transformer;
import com.musik.io.AudioReader;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class TransformTests {
    private static final Logger LOGGER = Logger.getLogger(Transformer.class);

    private static final Transformer TRANSFORMER = new Transformer();

    private static byte[] bytes;

    @BeforeClass
    public static void setUp() throws IOException {
        URL resource = TRANSFORMER.getClass().getResource("/sample.mp3");

        Preconditions.checkNotNull(resource, "Sample mp3 file does not exists");

        AudioReader reader = new AudioReader();
        bytes = reader.read(resource.getFile());
    }

    @Test
    public void testSimpleTransform() {
        ComplexNumber[] results = TRANSFORMER.transform(bytes, Transformer.DEFAULT_SIZE, false);

        if (LOGGER.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder("Fourier transformation results are : ");
            for (int i = 0; i < results.length; i++) {
                builder.append(results[i].getReal());
                builder.append(":");
                builder.append(results[i].getImaginary());
                builder.append("i, ");
            }

            LOGGER.debug(builder.toString());
            LOGGER.debug("Transformed output size " + results.length);
        }

        assertThat(results.length, greaterThan(0));
    }

    @Test
    public void testSensitiveTransform() {
        ComplexNumber[] results = TRANSFORMER.transform(bytes, 96, true);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Transformed output size " + results.length);
        }

        assertThat(results.length, greaterThan(0));
    }
}