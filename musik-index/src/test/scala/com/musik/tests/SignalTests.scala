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

import java.util.Properties

import com.google.common.io.ByteStreams
import com.musik.index.Transformer
import com.musik.io.AudioReader
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
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

  test("simple kafka write") {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("client.id", "musik")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")

    val size = (signal.length / Transformer.DEFAULT_SIZE) - 1

    val producer = new KafkaProducer[String, Array[Byte]](props)

    for (i <- 0 until size) {
      val ds = Transformer.DEFAULT_SIZE
      val slide = Transformer.DEFAULT_SIZE / 2

      // take a sample of the audio
      val range = signal.slice((i * ds) + slide, ((i + 1) * ds) + slide)

      val record = new ProducerRecord[String, Array[Byte]](topic, 1, title, range)

      producer.send(record)
    }

    producer.close()
  }
}