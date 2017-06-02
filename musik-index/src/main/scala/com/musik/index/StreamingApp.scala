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

import com.datastax.driver.core.Session
import com.musik.config.ConfigFactory
import com.musik.config.streaming.Config
import com.musik.index.functions.{AudioHash, PeakPoints, TimeToFrequency}
import com.musik.index.utils.Streaming
import com.musik.search.{CassandraCluster, RedisCluster}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

import scala.collection.JavaConversions._

object StreamingApp extends Streaming {
  /**
    * Loads matched signals from Cassandra server according to hash value
    *
    * @param cassandra the Cassandra cluster client
    * @param db        the db name
    * @param table     the table name
    * @param row       the matched signal hashes
    * @return the matched rows
    */
  def load(cassandra: CassandraCluster, db: String, table: String, row: Tuple): List[Tuple] = {
    var client: Session = null

    val sql = s"SELECT idx, name FROM $db.$table WHERE hash = ?"

    try {
      client = cassandra.getCluster.connect()

      val results = client.execute(sql, row.f0)

      results.all.map(r => new Tuple(row.f2, r.getInt(0), r.getString(1))).toList
    } finally {
      if (client != null) {
        client.close()
      }
    }
  }

  /**
    * Writes matches to Redis server
    *
    * @param redis the Redis cluster client
    * @param rows  the matched signals
    */
  def write(redis: RedisCluster, rows: List[Tuple]): Unit = {
    val client = redis.get

    for (i <- rows.indices) {
      val key = rows(i).f0

      client.hset(key, "idx", rows(i).f1.toString)
      client.hset(key, "name", rows(i).f2)

      if (logger.isDebugEnabled) {
        logger.debug(s"${rows(i).f0} matched ${rows(i).f2}")
      }
    }

    client.close()
  }

  def main(args: Array[String]): Unit = {
    // load configuration according to command line arguments
    val config = ConfigFactory.load(args, classOf[Config])

    val host = config.getHost
    val port = config.getPort.toInt
    val db = config.getDb
    val username = config.getUsername
    val password = config.getPassword
    val table = config.getTable
    val redisHost = config.getRedis
    val name = config.getClusterName

    try {
      lazy val redis = new RedisCluster(redisHost)

      lazy val cassandra = new CassandraCluster(host, port, username, password, name)

      // generate execution environment for the current streaming application
      val env = StreamExecutionEnvironment.getExecutionEnvironment

      val stream = env.addSource[StringBytePairs](getKafkaStream(config)).map(f => (f.f0, f.f1))

      val signals = stream.map(s => (s._1, s._2))

      val frequencies = signals.map(f => TimeToFrequency(f)).filter(f => f != null)

      val points = frequencies.map(f => PeakPoints(f))

      val samples = points.map(p => AudioHash(p)).flatMap(p => p).filter(h => h != null).map(s => toTuple(s))

      samples.map(s => load(cassandra, db, table, s)).map(s => write(redis, s))

      env.execute("musik streaming")
    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    }
  }
}