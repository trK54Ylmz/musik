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
import com.musik.config.index.Config
import com.musik.fs.BinaryFileInputFormat
import com.musik.index.functions.{AudioHash, PeakPoints, TimeToFrequency}
import com.musik.index.utils.Types._
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.hadoop.mapreduce.HadoopInputFormat
import org.apache.flink.api.java.typeutils.TupleTypeInfo
import org.apache.flink.api.scala.{ExecutionEnvironment, _}
import org.apache.flink.batch.connectors.cassandra.CassandraOutputFormat
import org.apache.flink.hadoopcompatibility.HadoopInputs
import org.apache.hadoop.io.{BytesWritable, Text}

object IndexApp extends BaseApp {
  /**
    * Creates hadoop input format for reading binary signals
    *
    * @param input the input path of coming or cold signals
    * @return the hadoop input format for flink data sets
    */
  def getFormat(input: String): HadoopInputFormat[Text, BytesWritable] = {
    val key = classOf[Text]
    val value = classOf[BytesWritable]

    // hadoop key type information that represents file name etc.
    val stringInfo: TypeInformation[Text] = TypeInformation.of(key)

    // hadoop value type information that represents signal content as byte array etc.
    val bytesInfo: TypeInformation[BytesWritable] = TypeInformation.of(value)

    // hadoop input type description
    implicit val typeInfo: TupleTypeInfo[TupleTwo] = new TupleTypeInfo[TupleTwo](stringInfo, bytesInfo)

    HadoopInputs.readHadoopFile(new BinaryFileInputFormat, key, value, input)
  }

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
      val signals = source.map(f => (f.f0.toString, getSignal(f.f1.getBytes)))

      // transform time domain signals to frequency domain
      val frequencies = signals.map(f => TimeToFrequency(f)).filter(_ != null)

      // find peak points of signal magnitudes
      val points = frequencies.map(PeakPoints(_))

      // generate hashes from magnitudes and generate row tuples from hashes
      val rows = points.map(p => AudioHash(p)).flatMap(p => p).map(s => toTuple(s))

      if (config.getTest != 1) {
        val sql = s"INSERT INTO $db.$table (hash, idx, name) VALUES (?, ?, ?)"
        val cluster = getCluster(host, port, username, password)

        // write tuples
        rows.output(new CassandraOutputFormat[TupleThree](sql, cluster))
      } else {
        rows.print()
      }

      env.execute("musik batch")
    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    }
  }
}