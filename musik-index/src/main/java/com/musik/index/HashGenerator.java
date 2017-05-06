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

public class HashGenerator {
    private static final String DIGIT_FORMAT = "%03d";

    private static String toHash(double value) {
        return String.format(DIGIT_FORMAT, (int) value);
    }

    /**
     * Generates comparable hashes for music
     *
     * @param numbers the peek points of audio signal
     * @return the hash group of samples
     */
    public String[] generate(byte[][] numbers) {
        String[] hashes = new String[numbers.length];

        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] == null) {
                continue;
            }

            StringBuilder buffer = new StringBuilder();

            for (int j = 0; j < numbers[i].length; j++) {
                buffer.append(toHash(numbers[i][j]));
            }

            hashes[i] = buffer.toString();
        }

        return hashes;
    }
}