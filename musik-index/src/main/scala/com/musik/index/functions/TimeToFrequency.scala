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

package com.musik.index.functions

import com.musik.index.{ComplexNumber, Transformer}

object TimeToFrequency {
  private[this] val transformer = new Transformer

  /**
    * Returns the file name without extension
    *
    * @param name the name of the file to trim the extension from
    * @return the file name without its path or extension
    */
  def getName(name: String): String = {
    val index = name.lastIndexOf('.')

    if (index == -1) name else name.substring(0, index)
  }

  /**
    * Converts Hadoop input to frequency based complex numbers
    *
    * @param obj the input that contains audio content
    * @return the complex numbers array which is converted to frequency domain
    */
  def apply(obj: (String, Array[Byte])): (String, Array[Array[ComplexNumber]]) = {
    if (obj == null || obj._2 == null) {
      return null
    }

    val name = getName(obj._1.toString)
    val frequencyData = transformer.transform(obj._2, Transformer.DEFAULT_SIZE)

    (name, frequencyData)
  }
}