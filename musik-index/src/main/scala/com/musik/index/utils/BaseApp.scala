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

import java.io.ByteArrayInputStream

import com.datastax.driver.core.Cluster
import com.musik.db.entity.SongContent
import com.musik.io.AudioReader
import org.apache.flink.api.java.tuple.Tuple3
import org.apache.flink.streaming.connectors.cassandra.ClusterBuilder
import org.apache.log4j.Logger

trait BaseApp {
  private[index] val logger: Logger = Logger.getLogger(getClass)

  private[index] val reader = new AudioReader()

  type Tuple = Tuple3[String, Int, String]

  /**
    * Converts song entity to flink tuple
    *
    * @param song the song entity
    * @return the flink tuple
    */
  def toTuple(song: SongContent): Tuple = new Tuple(song.getHash, song.getIdx, song.getName)

  /**
    * Generates cluster builder
    *
    * @param host the host address of cassandra node
    * @param port the port of cassandra node
    * @param user the username for cluster authentication
    * @param pass the password for cluster authentication
    * @return the cluster builder
    */
  def getCluster(host: String, port: Int, user: String, pass: String): ClusterBuilder = {
    new ClusterBuilder {
      override def buildCluster(builder: Cluster.Builder): Cluster = {
        builder.addContactPoints(host).withPort(port).withCredentials(user, pass).build()
      }
    }
  }

  /**
    * Returns content of audio signal
    *
    * @param bytes the encoded audio signal
    * @return the content of audio signal
    */
  def getSignal(bytes: Array[Byte]): Array[Byte] = {
    reader.read(new ByteArrayInputStream(bytes))
  }
}