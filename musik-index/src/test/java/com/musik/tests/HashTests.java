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
import com.musik.index.Eliminator;
import com.musik.index.HashGenerator;
import com.musik.index.Transformer;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HashTests extends TestUtil {
    private static final Logger LOGGER = Logger.getLogger(TestUtil.class);

    private static final Transformer TRANSFORMER = new Transformer();

    private static final Eliminator ELIMINATOR = new Eliminator();

    private static final HashGenerator HASH_GENERATOR = new HashGenerator();

    @Test
    public void testSimple() {
        ComplexNumber[][] numbers = TRANSFORMER.transform(bytes, Transformer.DEFAULT_SIZE);
        byte[][] eliminated = ELIMINATOR.eliminate(numbers);
        String[] hashes = HASH_GENERATOR.generate(eliminated);

        for (int i = 0; i < hashes.length; i++) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(Utils.s("The {0}th hash is {1}", i + 1, hashes[i]));
            }
        }
    }
}