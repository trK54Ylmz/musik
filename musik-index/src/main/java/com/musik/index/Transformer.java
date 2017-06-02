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

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;

import com.musik.Utils;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.log4j.Logger;

public class Transformer {
    private static final Logger LOGGER = Logger.getLogger(Transformer.class);

    private static final FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);

    public static final int DEFAULT_SIZE = Utils.SAMPLE_SIZE;

    /**
     * Executes fast fourier transformation to given input array
     *
     * @param bytes the time based input
     * @param size  the sample size
     * @return frequency based output data
     * @see <a href="https://goo.gl/hAzIYQ">https://en.wikipedia.org/wiki/Fast_Fourier_transform</a>
     */
    public ComplexNumber[][] transform(byte[] bytes, int size) {
        Preconditions.checkArgument(size > 0, "Sample size must be greater than zero");
        Preconditions.checkArgument(bytes.length > 0, "Byte array is empty");

        // log_2 {size}
        double sizeCheck = Math.log(size) / Math.log(2);

        if (sizeCheck != (int) sizeCheck) {
            throw new IllegalStateException(Utils.s("{0} is not a power of 2", size));
        }

        double log = (bytes.length / size);

        // complex array size must be power of two
        if (log != (int) log) {
            log = 1 + (int) log;

            int newSize = size * (int) log;
            byte[] temp = new byte[newSize];

            for (int i = 0; i < newSize; i++) {
                if (i < bytes.length) {
                    temp[i] = bytes[i];
                } else {
                    temp[i] = 0;
                }
            }

            bytes = temp;
        }

        // sampling interval
        int sampleSize = bytes.length / size;

        if (LOGGER.isDebugEnabled()) {
            String hash = Hashing.md5().hashBytes(bytes).toString().intern();

            LOGGER.debug(Utils.s("Sample size is {0} for ''{1}''", size, hash));
        }

        ComplexNumber[][] results = new ComplexNumber[sampleSize][];

        for (int i = 0; i < sampleSize; i++) {
            Complex[] complexes = new Complex[size];

            for (int j = 0; j < size; j++) {
                complexes[j] = new Complex(bytes[(size * i) + j], 0);
            }

            Complex[] transform = FFT.transform(complexes, TransformType.FORWARD);

            for (int j = 0, length = transform.length / 2 + 1; j < length; j++) {
                if (j == 0) {
                    results[i] = new ComplexNumber[length];
                }

                results[i][j] = new ComplexNumber(transform[j].getReal(), transform[j].getImaginary());
            }
        }

        return results;
    }
}