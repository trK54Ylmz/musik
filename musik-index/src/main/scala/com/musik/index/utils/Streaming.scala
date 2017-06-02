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

package com.musik.index.utils

import java.util.Properties

import com.musik.config.streaming.Config
import org.apache.commons.io.Charsets
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.api.java.typeutils.TupleTypeInfo
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema

class Streaming extends BaseApp {
  type StringBytePairs = Tuple2[String, Array[Byte]]

  val schema = new KeyedDeserializationSchema[StringBytePairs] {
    // kafka key type information
    val stringInfo: TypeInformation[String] = TypeInformation.of(classOf[String])

    // kafka value type information
    val bytesInfo: TypeInformation[Array[Byte]] = TypeInformation.of(classOf[Array[Byte]])

    // kafka io format description
    val typeInfo = new TupleTypeInfo[StringBytePairs](stringInfo, bytesInfo)

    /**
      * Method to decide whether the element signals the end of the stream. If
      * true is returned the element won't be emitted
      *
      * @param element the
      * @return TRUE if element signal end of the stream, FALSE otherwise
      */
    override def isEndOfStream(element: StringBytePairs): Boolean = false

    /**
      * De-serializes the byte message
      *
      * @param keys      the key as byte array
      * @param values    the value as byte array
      * @param topic     the name of the topic
      * @param partition the kafka partition
      * @param offset    the kafka offset
      * @return the de-serialized message
      */
    override def deserialize(keys: Array[Byte],
                             values: Array[Byte],
                             topic: String,
                             partition: Int,
                             offset: Long): StringBytePairs = {
      Tuple2.of(new String(keys, Charsets.UTF_8), values)
    }

    /**
      * The type definition
      *
      * @return the type definition
      */
    override def getProducedType: TypeInformation[StringBytePairs] = typeInfo
  }

  /**
    * Creates Kafka topic parameters
    *
    * @param config the application configuration
    * @return the kafka parameters
    */
  def getParams(config: Config): Properties = {
    val properties = new Properties
    properties.setProperty("bootstrap.servers", config.getKafkaServers)
    properties.setProperty("zookeeper.connect", config.getZookeeperServers)
    properties.setProperty("group.id", config.getGroupId)

    properties
  }

  /**
    * Generates Kafka stream source
    *
    * @param config the configuration class that generated for application
    * @return the Kafka 0.10.x consumer
    */
  def getKafkaStream(config: Config): FlinkKafkaConsumer010[StringBytePairs] = {
    val topics = config.getKafkaTopics
    val properties = getParams(config)

    new FlinkKafkaConsumer010[StringBytePairs](topics, schema, properties)
  }
}