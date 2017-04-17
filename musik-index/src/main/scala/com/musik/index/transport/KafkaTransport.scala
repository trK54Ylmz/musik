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

package com.musik.index.transport

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.BytesDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.codehaus.jackson.map.deser.std.StringDeserializer

class KafkaTransport(config: Properties) {
  def getParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> config.getProperty("kafka_servers"),
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[BytesDeserializer],
    "group.id" -> "use_a_separate_group_id_for_each_stream",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  def getKafkaStream(streaming: StreamingContext): InputDStream[ConsumerRecord[String, Array[Byte]]] = {
    val topics = config.getProperty("topics").split(",")

    KafkaUtils.createDirectStream[String, Array[Byte]](
      streaming,
      PreferConsistent,
      Subscribe[String, Array[Byte]](topics, getParams)
    )
  }
}
