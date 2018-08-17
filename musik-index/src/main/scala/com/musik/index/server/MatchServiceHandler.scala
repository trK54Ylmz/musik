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

package com.musik.index.server

import java.nio.ByteBuffer
import java.util
import com.datastax.driver.core.Session
import com.musik.config.server.Config
import com.musik.index.ServerApp.toTuple
import com.musik.index.functions.{AudioHash, PeakPoints, TimeToFrequency}
import com.musik.index.utils.Types.TupleThree
import com.musik.search.CassandraCluster
import com.musik.service.MatchService
import org.apache.flink.api.scala.ExecutionEnvironment

import scala.collection.JavaConversions._
import org.apache.flink.api.scala._

class MatchServiceHandler(val config: Config, val env: ExecutionEnvironment) extends MatchService.Iface {
  val host: String = config.getHost

  val port: Int = config.getPort.toInt

  val db: String = config.getDb

  val username: String = config.getUsername

  val password: String = config.getPassword

  val table: String = config.getTable

  val name: String = config.getClusterName

  implicit lazy val cassandra: CassandraCluster = new CassandraCluster(host, port, username, password, name)

  /**
    * Loads matched signals from Cassandra server according to hash value
    *
    * @param db        the db name
    * @param table     the table name
    * @param row       the matched signal hashes
    * @param cassandra the Cassandra cluster client
    * @return the matched rows
    */
  def load(db: String, table: String, row: TupleThree)(implicit cassandra: CassandraCluster): List[TupleThree] = {
    var client: Session = null

    val sql = s"SELECT idx, name FROM $db.$table WHERE hash = ?"

    try {
      client = cassandra.getCluster.connect()

      val results = client.execute(sql, row.f0)

      results.all.map(r => new TupleThree(row.f2, r.getInt(0), r.getString(1))).toList
    } finally {
      if (client != null) {
        client.close()
      }
    }
  }

  /**
    * Thrift service handler
    * <p/>
    * Handles audio signals and finds songs according to content of signals
    *
    * @param buffer the audio signal as buffer
    * @return the list of song hashes
    */
  override def find(buffer: ByteBuffer): util.List[String] = {
    val byteArray: Array[Byte] = buffer.array()

    // create Kafka stream as a source
    val input = env.fromElements(byteArray).map(f => ("", f))

    // convert Flink's java tuples to scala tuples
    val signals = input.map(s => (s._1, s._2))

    // transform time domain signals to frequency domain
    val frequencies = signals.map(f => TimeToFrequency(f)).filter(_ != null)

    // find peak points of signal magnitudes
    val points = frequencies.map(f => PeakPoints(f))

    // generate hashes from magnitudes and generate row tuples from hashes
    val samples = points.map(p => AudioHash(p)).flatMap(p => p).filter(_ != null).map(s => toTuple(s))

    // load results from cassandra
    val ds = samples.map(s => load(db, table, s))

    // write results to redis server
    ds.flatMap(k => k).map(k => k.f0).collect()
  }

  /**
    * Closes active database connection
    */
  def close(): Unit = {
    cassandra.close()
  }
}
