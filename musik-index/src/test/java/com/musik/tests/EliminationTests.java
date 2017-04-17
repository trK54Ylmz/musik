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

import com.musik.index.ComplexNumber;
import com.musik.index.Eliminator;
import com.musik.index.Transformer;

import org.apache.log4j.Logger;
import org.junit.Test;

public class EliminationTests extends TestUtil {
    protected static final Logger LOGGER = Logger.getLogger(TestUtil.class);

    protected static final Transformer TRANSFORMER = new Transformer();

    private static final Eliminator ELIMINATOR = new Eliminator();

    @Test
    public void testSimple() {
        ComplexNumber[][] numbers = TRANSFORMER.transform(bytes, Transformer.DEFAULT_SIZE);

        ComplexNumber[][] eleminated = ELIMINATOR.eliminate(numbers);


    }
}