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

package com.musik.index;

import com.musik.Utils;

import java.util.Arrays;

public class Eliminator {
    private final int EXPAND = 24;

    /**
     * Checks the magnitudes
     *
     * @return TRUE if all magnitudes are zero, FALSE otherwise
     */
    private boolean isIncorrect(byte[] magnitudes) {
        int count = 0;

        for (int i = 0; i < magnitudes.length; i++) {
            count += magnitudes[i];
        }

        return count / magnitudes.length < EXPAND;
    }

    /**
     * Eliminates
     *
     * @param points the frequency points
     * @return the peek points of transformed signals
     */
    public byte[][] eliminate(ComplexNumber[][] points) {
        byte[][] eliminated = new byte[points.length][];

        for (int i = 0; i < points.length; i++) {
            byte[] sample = new byte[points[i].length];

            for (int j = 0; j < points[i].length; j++) {
                double real = points[i][j].getReal();
                double imaginary = points[i][j].getImaginary();
                double magnitude = real * real + imaginary * imaginary;

                magnitude = Math.sqrt(magnitude);

                sample[j] = (byte) (EXPAND * Math.log10(magnitude));
            }

            if (isIncorrect(sample)) {
                continue;
            }

            Arrays.sort(sample);

            eliminated[i] = Arrays.copyOfRange(sample, sample.length - Utils.HASH_SIZE, sample.length);
        }

        return eliminated;
    }
}