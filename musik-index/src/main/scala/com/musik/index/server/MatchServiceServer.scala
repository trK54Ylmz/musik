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

import com.musik.service.MatchService
import org.apache.thrift.server.{TNonblockingServer, TServer}
import org.apache.thrift.transport.{TNonblockingServerSocket, TNonblockingServerTransport}

class MatchServiceServer(handler: MatchServiceHandler) {
  private[this] val processor = new MatchService.Processor[MatchServiceHandler](handler)

  private[this] var transport: TNonblockingServerTransport = _

  private[this] var server: TServer = _

  /**
    * Start thrift server
    */
  def start(port: Int): Unit = {
    transport = new TNonblockingServerSocket(port)
    server = new TNonblockingServer(new TNonblockingServer.Args(transport).processor(processor))
  }

  /**
    * Stop thrift server
    */
  def stop(): Unit = {
    if (server == null) return

    server.stop()
    handler.close()
  }
}
