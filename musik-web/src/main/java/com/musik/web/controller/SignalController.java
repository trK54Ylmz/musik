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

package com.musik.web.controller;

import com.google.common.io.ByteSource;
import com.musik.Utils;
import com.musik.config.Config;
import com.musik.config.ConfigFactory;
import com.musik.io.AudioReader;
import com.musik.service.MatchService;
import com.musik.web.response.SignalMatchResponse;
import com.musik.web.response.SignalSampleResponse;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/signal")
public class SignalController {
    private static final Logger LOGGER = Logger.getLogger(SignalController.class);

    private static final AudioReader READER = new AudioReader();

    private final MatchService.Client client;

    public SignalController() {
        String host = "localhost";
        int port = 8081;

        try (TTransport transport = new TSocket(host, port)) {
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            client = new MatchService.Client(protocol);
        } catch (TTransportException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequestMapping("/sample")
    @ResponseBody
    public SignalSampleResponse getSampleValue() {
        return new SignalSampleResponse(true, Utils.SAMPLE_SIZE);
    }

    @RequestMapping("/match")
    @ResponseBody
    public SignalMatchResponse uploadChunk(@RequestBody String data) {
        SignalMatchResponse response = new SignalMatchResponse(false);

        try {
            if (!data.startsWith("data:audio/mp3")) {
                response.setMessage("Invalid request body");

                return response;
            }

            // convert byte array to audio signals
            byte[] content = Base64.getDecoder().decode(data.substring(22));
            byte[] signals = READER.read(ByteSource.wrap(content).openStream());

            // send signals to server
            List<String> idGroup = client.find(ByteBuffer.wrap(signals));

            // convert song names to entity object
            List<SignalMatchResponse.Song> songs = idGroup.stream()
                    .map(SignalMatchResponse.Song::new)
                    .collect(Collectors.toList());

            response.setMatches(songs);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return response;
    }
}