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

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class Transformer {
    private static final FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);

    public static final int DEFAULT_SIZE = 4096;

    /**
     * Executes fast fourier transformation to given input array
     *
     * @param bytes the time based input
     * @param size  the sample size
     * @param trim  approximate a number if sample size is a number which is not power of two
     * @return frequency based output data
     * @see <a href="https://goo.gl/hAzIYQ">https://en.wikipedia.org/wiki/Fast_Fourier_transform</a>
     */
    public ComplexNumber[] transform(byte[] bytes, int size, boolean trim) {
        Preconditions.checkArgument(size > 0, "Sample size must be greater than zero");
        Preconditions.checkArgument(bytes.length > 0, "Byte array is empty");

        // log_2 {size}
        double log = (Math.log(size) / Math.log(2));

        // complex array size must be power of two
        if (log != (int) log) {
            if (trim) {
                int iterationSize = 1000;
                int real = size;

                for (int i = 2; i < iterationSize; i++) {
                    if (Math.pow(2, i) > size) {
                        size = (int) Math.pow(2, i - 1);

                        break;
                    }
                }

                // probably iteration limit exceed
                if (size == real || size > Integer.MAX_VALUE - 2) {
                    String formatted = String.format("Sample array size '%d' is not a power of two " +
                            "and approximated number '%d' is too big", real, size);

                    throw new RuntimeException(formatted);
                }
            } else {
                throw new NumberFormatException("Sample array size must be power of two");
            }
        }

        int sampleSize = bytes.length / size;

        ComplexNumber[] results = new ComplexNumber[sampleSize];
        Complex[] complexes = new Complex[size];

        for (int i = 0; i < sampleSize; i++) {
            for (int j = 0; j < size; j++) {
                complexes[j] = new Complex(bytes[(size * i) + j], 0);
            }

            Complex[] transform = FFT.transform(complexes, TransformType.FORWARD);

            for (int j = 0; j < transform.length; j++) {
                results[i] = new ComplexNumber(transform[j].getReal(), transform[j].getImaginary());
            }
        }

        return results;
    }
}