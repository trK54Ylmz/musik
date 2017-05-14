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

import com.musik.config.ConfigFactory
import com.musik.config.streaming.Config
import com.musik.index.BatchApp.getCluster
import com.musik.index.functions.{AudioHash, PeakPoints, TimeToFrequency}
import com.musik.index.transport.KafkaTransport
import org.apache.flink.batch.connectors.cassandra.CassandraInputFormat
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

object StreamingApp extends KafkaTransport {
  def main(args: Array[String]): Unit = {
    // load configuration according to command line arguments
    val config = ConfigFactory.load(args, classOf[Config])

    val host = config.getHost
    val port = config.getPort.toInt
    val db = config.getDb
    val username = config.getUsername
    val password = config.getPassword
    val table = config.getTable

    try {
      // generate execution environment for the current streaming application
      val env = StreamExecutionEnvironment.getExecutionEnvironment

      val stream = env.addSource[StringBytePairs](getKafkaStream(config)).map(f => (f.f0, f.f1))

      val frequencies = stream.map(f => TimeToFrequency(f)).filter(f => f != null)
      val points = frequencies.map(f => PeakPoints(f))
      val samples = points.map(p => AudioHash(p)).flatMap(p => p).map(s => toTuple(s))

      val sql = s"SELECT hash, idx, value FROM $db.$table WHERE hash = ?"
      val cluster = getCluster(host, port, username, password)

      val data = new CassandraInputFormat[Tuple](sql, cluster)


    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    }
  }
}