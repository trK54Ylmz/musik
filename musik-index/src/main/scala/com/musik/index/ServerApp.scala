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
import com.musik.config.server.Config
import com.musik.index.server.{MatchServiceHandler, MatchServiceServer}
import org.apache.flink.api.scala.ExecutionEnvironment


object ServerApp extends BaseApp {
  def main(args: Array[String]): Unit = {
    // load configuration according to command line arguments
    val config = ConfigFactory.load(args, classOf[Config])

    try {
      // generate execution environment for the current application
      val env = ExecutionEnvironment.getExecutionEnvironment

      val handler: MatchServiceHandler = new MatchServiceHandler(config, env)

      // create and start rpc server
      val server: MatchServiceServer = new MatchServiceServer(handler)
      server.start(config.getRpcPort)

      val thread = new Thread {
        override def run(): Unit = {
          env.execute("musik query app")
        }
      }

      // close rpc server
      sys.addShutdownHook {
        server.stop()

        if (thread.isAlive) {
          thread.interrupt()
        }
      }
    } catch {
      case t: Throwable => logger.fatal(t.getMessage, t)
    }
  }
}