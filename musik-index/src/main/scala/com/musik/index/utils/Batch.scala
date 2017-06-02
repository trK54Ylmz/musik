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

import com.musik.fs.BinaryFileInputFormat
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.hadoop.mapreduce.HadoopInputFormat
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.api.java.typeutils.TupleTypeInfo
import org.apache.flink.hadoopcompatibility.HadoopInputs
import org.apache.hadoop.io.{BytesWritable, Text}

class Batch extends BaseApp {
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
    implicit val typeInfo = new TupleTypeInfo[Tuple2[Text, BytesWritable]](stringInfo, bytesInfo)

    HadoopInputs.readHadoopFile(new BinaryFileInputFormat, key, value, input)
  }
}