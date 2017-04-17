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

import com.musik.Utils;
import com.musik.index.ComplexNumber;
import com.musik.index.Transformer;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@RunWith(SystemPropertyRunner.class)
public class TransformTests extends TestUtil {
    private static final Logger LOGGER = Logger.getLogger(Transformer.class);

    private static final Transformer TRANSFORMER = new Transformer();

    @Test
    public void testSimpleTransform() {
        byte[] range = {1, 1, 1, 1, 1, 1, 1, 1};

        ComplexNumber[][] results = TRANSFORMER.transform(range, range.length);

        assertThat(results.length, greaterThan(0));

        // the first complex number of FFT array must be equal to 8.0
        assertThat(results[0][0].abs(), equalTo(8.0));
    }

    @Test
    public void testSimpleFileTransform() {
        byte[] range = Arrays.copyOfRange(bytes, 0, Transformer.DEFAULT_SIZE);

        ComplexNumber[][] results = TRANSFORMER.transform(range, Transformer.DEFAULT_SIZE);

        if (LOGGER.isDebugEnabled()) {
            for (int i = 0; i < results.length; i++) {
                StringBuilder builder = new StringBuilder(Utils.s("Transformation for {0} : ", i));

                for (int j = 0; j < Math.min(20, results[i].length); j++) {
                    builder.append(String.format("%1.2f", results[i][j].getReal()));
                    builder.append(" ");
                    builder.append(String.format("%1.2f", results[i][j].getImaginary()));
                    builder.append("i, ");
                }

                LOGGER.debug(builder.toString());
            }

            LOGGER.debug("Transformed sample size " + results.length);
        }

        assertThat(results.length, greaterThan(0));
    }

    @Exclusive
    @Test
    public void drawSimpleTransformedWaveForm() throws IOException {
        int size = bytes.length;

        double log = Math.log(bytes.length) / Math.log(2);
        if (log != (int) log) {
            size = (int) Math.pow(2, (int) log + 1);
        }

        byte[] range;
        if (size == bytes.length) {
            range = Arrays.copyOfRange(bytes, 0, bytes.length);
        } else {
            range = new byte[size];

            for (int i = 0; i < size; i++) {
                if (i < bytes.length) {
                    range[i] = bytes[i];
                } else {
                    range[i] = 0;
                }
            }
        }

        ComplexNumber[][] results = TRANSFORMER.transform(range, size);

        byte[] flat = new byte[results.length * results[0].length];

        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < results[i].length; j++) {
                flat[i + j] = (byte) Math.sqrt(Math.pow(results[i][j].getReal(), 2) + Math.pow(results[i][j].getImaginary(), 2));
            }
        }

        double sample = bytes.length / flat.length;

        LOGGER.debug(Utils.s("data : {0}, fft : {1}, sample : {2}", bytes.length, flat.length, sample));

        draw(flat, "output-fft");
    }

    @Exclusive
    @Test
    public void drawSampledTransformedWaveForm() throws IOException {
        byte[] range = Arrays.copyOfRange(bytes, 0, bytes.length);

        ComplexNumber[][] results = TRANSFORMER.transform(range, Transformer.DEFAULT_SIZE);
        for (int i = 0; i < results.length; i++) {
            byte[] flat = new byte[results[i].length];

            for (int j = 0; j < results[i].length; j++) {
                flat[j] = (byte) Math.sqrt(Math.pow(results[i][j].getReal(), 2) + Math.pow(results[i][j].getImaginary(), 2));
            }

            double sample = bytes.length / flat.length;

            LOGGER.debug(Utils.s("data : {0}, fft : {1}, sample : {2}", bytes.length, flat.length, sample));

            draw(flat, "output-sampled-fft-" + (i + 1));
        }
    }
}