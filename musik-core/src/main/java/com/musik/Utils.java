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

package com.musik;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.MessageFormat;

public class Utils {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static final int SAMPLE_SIZE = 32768;

    public static final int HASH_SIZE = 32;

    /**
     * Formats given text according to parameters
     *
     * @param text the format text
     * @param args the parameters to bind format text
     * @return formatted text
     */
    public static String s(String text, Object... args) {
        if (text == null) {
            return null;
        }

        return MessageFormat.format(text, args);
    }

    /**
     * Formats current date time
     *
     * @return formatted current date time
     */
    public static String now() {
        return TIME_FORMAT.print(System.currentTimeMillis());
    }
}