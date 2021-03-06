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

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class AudioFileTests extends TestUtil {
    @Test
    public void testRead() throws IOException {
        assertThat(bytes.length, greaterThan(0));
    }

    @Test
    public void drawWaveForm() throws IOException {
        int[] numbers = new int[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            numbers[i] = (int) bytes[i];
        }

        draw(numbers, "original-output");
    }
}