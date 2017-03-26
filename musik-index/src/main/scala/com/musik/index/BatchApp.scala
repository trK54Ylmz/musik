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

import java.util.Properties

import com.musik.db.entity.SongContent
import com.musik.fs.BinaryFileInputFormat
import com.musik.index.functions.{AudioHash, PeakPoints, TimeToFrequency}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.sql.SparkSession

object BatchApp extends App {
  def main(args: Array[String]): Unit = {
    var spark: SparkSession = null

    val input = config.getProperty("input")
    val host = config.getProperty("host")
    val port = config.getProperty("port")
    val db = config.getProperty("db")
    val username = config.getProperty("username")
    val password = config.getProperty("username")
    val table = config.getProperty("table")

    val url = s"jdbc:mysql://$host:$port/$db"

    val properties = new Properties()
    properties.setProperty("driver", "com.mysql.jdbc.Driver")
    properties.setProperty("user", username)
    properties.setProperty("password", password)

    try {
      spark = SparkSession.builder().master(getMaster).getOrCreate()

      // read audio files from file system
      val audios = spark
        .sparkContext
        .newAPIHadoopFile[Text, BytesWritable, BinaryFileInputFormat](input)

      val frequencies = audios.map(f => TimeToFrequency(f)).filter(f => f != null)

      val hashes = frequencies.map(p => PeakPoints(p)).map(p => AudioHash(p)).flatMap(p => p)

      val rows = spark.createDataFrame(hashes, classOf[SongContent])

      rows.write.jdbc(url, table, properties)
    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    } finally {
      // shutdown spark session
      if (spark != null) {
        spark.stop
      }
    }
  }
}