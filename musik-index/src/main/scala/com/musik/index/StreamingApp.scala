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
import com.fasterxml.jackson.databind.ObjectMapper
import com.musik.config.ConfigFactory
import com.musik.config.streaming.Config
import com.musik.index.functions.{AudioHash, PeakPoints, TimeToFrequency}
import com.musik.index.utils.Streaming
import com.musik.search.{CassandraCluster, RedisCluster}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import redis.clients.jedis.Jedis

import scala.collection.JavaConversions._

object StreamingApp extends Streaming {
  private[this] val mapper = new ObjectMapper()

  /**
    * Loads matched signals from Cassandra server according to hash value
    *
    * @param db        the db name
    * @param table     the table name
    * @param row       the matched signal hashes
    * @param cassandra the Cassandra cluster client
    * @return the matched rows
    */
  def load(db: String, table: String, row: Tuple)(implicit cassandra: CassandraCluster): List[Tuple] = {
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
    * @param rows  the matched signals
    * @param redis the Redis cluster client
    */
  def write(rows: List[Tuple])(implicit redis: RedisCluster): Unit = {
    var client: Jedis = null

    try {
      client = redis.get()

      for (i <- rows.indices) {
        val key = rows(i).f0

        client.sadd(key, mapper.writer.writeValueAsString(rows(i)))

        if (logger.isDebugEnabled) {
          logger.debug(s"${rows(i).f0} matched ${rows(i).f2}")
        }
      }
    } finally {
      if (client != null) {
        client.close()
      }
    }
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
    val redisConfig = config.getRedis
    val name = config.getClusterName

    try {
      implicit lazy val redis = new RedisCluster(redisConfig)

      implicit lazy val cassandra = new CassandraCluster(host, port, username, password, name)

      // close redis and cassandra connection
      sys.addShutdownHook {
        cassandra.close()

        redis.close()
      }

      // generate execution environment for the current streaming application
      val env = StreamExecutionEnvironment.getExecutionEnvironment

      // create Kafka stream as a source
      val stream = env.addSource[StringBytePairs](getKafkaStream(config)).map(f => (f.f0, f.f1))

      // convert flink's java tuples to scala tuples
      val signals = stream.map(s => (s._1, s._2))

      // transform time domain signals to frequency domain
      val frequencies = signals.map(f => TimeToFrequency(f)).filter(f => f != null)

      // find peak points of signal magnitudes
      val points = frequencies.map(f => PeakPoints(f))

      // generate hashes from magnitudes and generate row tuples from hashes
      val samples = points.map(p => AudioHash(p)).flatMap(p => p).filter(h => h != null).map(s => toTuple(s))

      // load results from cassandra
      val ds = samples.map(s => load(db, table, s))

      if (config.getTest != 1) {
        // write results to redis server
        ds.map(s => write(s))
      } else {
        ds.print()
      }

      env.execute("musik streaming")
    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    }
  }
}