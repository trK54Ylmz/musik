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
import com.musik.config.batch.Config
import com.musik.index.functions.{AudioHash, PeakPoints, TimeToFrequency}
import com.musik.index.transport.CassandraTransport
import org.apache.flink.api.scala.{ExecutionEnvironment, _}
import org.apache.flink.batch.connectors.cassandra.CassandraOutputFormat

object BatchApp extends CassandraTransport {
  def main(args: Array[String]): Unit = {
    // load configuration according to command line arguments
    val config = ConfigFactory.load(args, classOf[Config])

    val input = config.getInput
    val host = config.getHost
    val port = config.getPort.toInt
    val db = config.getDb
    val username = config.getUsername
    val password = config.getPassword
    val table = config.getTable

    try {
      // generate execution environment for the current application
      val env = ExecutionEnvironment.getExecutionEnvironment

      // create binary hadoop input format as a source
      val source = env.createInput(getFormat(input))

      // convert flink's java tuples to scala tuples
      val signals = source.map(f => (f.f0.toString, f.f1.getBytes))

      // transform time domain signals to frequency domain
      val frequencies = signals.map(f => TimeToFrequency(f)).filter(f => f != null)

      // find peak points of signal magnitudes
      val points = frequencies.map(f => PeakPoints(f))

      // generate hashes from magnitudes and generate row tuples from hashes
      val rows = points.map(p => AudioHash(p)).flatMap(p => p).map(s => toTuple(s))

      val sql = s"INSERT INTO $db.$table (hash, idx, name) VALUES (?, ?, ?)"
      val cluster = getCluster(host, port, username, password)

      // write tuples
      rows.output(new CassandraOutputFormat[Tuple](sql, cluster))

      env.execute()
    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    }
  }
}