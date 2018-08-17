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

package com.musik.tests

import com.google.common.io.ByteStreams
import com.musik.io.AudioReader
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class SignalTests extends FunSuite with BeforeAndAfter with Matchers {
  // audio signals
  var signal: Array[Byte] = _

  // whole audio file content (with metadata etc.)
  var data: Array[Byte] = _

  // kafka topic name
  val topic = "musik"

  // simple audio id
  val title = "test"

  before {
    val reader = new AudioReader

    val path = classOf[SignalTests].getResource("/sample.mp3").getFile
    val stream = classOf[SignalTests].getResourceAsStream("/sample.mp3")

    signal = reader.read(path)

    data = ByteStreams.toByteArray(stream)
  }
}