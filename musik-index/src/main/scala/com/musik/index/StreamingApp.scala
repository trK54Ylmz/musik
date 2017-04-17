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

package com.musik.index

import com.musik.config.{ConfigFactory, Configs}
import com.musik.index.transport.KafkaTransport
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingApp extends App {
  def main(args: Array[String]): Unit = {
    var spark: SparkSession = null
    val streaming: StreamingContext = null

    val config = ConfigFactory.build().set(Configs.INDEX).load()

    try {
      spark = SparkSession.builder()
        .master(getMaster)
        .getOrCreate()

      val streaming = new StreamingContext(spark.sparkContext, Seconds(10))

      val kafka = new KafkaTransport(config)

      val stream = kafka.getKafkaStream(streaming)

      stream.map(d => (d.key(), d.value()))
    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    } finally {
      // shutdown streaming context
      if (streaming != null) {
        streaming.awaitTermination()
      }

      // shutdown spark session
      if (spark != null) {
        spark.stop
      }
    }
  }
}