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

import com.google.common.base.Preconditions;

import com.musik.Utils;
import com.musik.io.AudioReader;
import com.musik.web.response.SignalContentResponse;
import com.musik.web.response.SignalSampleResponse;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/signal")
public class SignalRequestController {
    private static final Logger LOGGER = Logger.getLogger(SignalRequestController.class);

    private static final int LIMIT = 4096;

    @RequestMapping("/sample")
    @ResponseBody
    public SignalSampleResponse getSampleValue() {
        return new SignalSampleResponse(true, Utils.SAMPLE_SIZE);
    }

    @RequestMapping(value = "/test/content")
    @ResponseBody
    public SignalContentResponse getContentOfFile(@RequestParam("file") String filePath) {
        Preconditions.checkNotNull(filePath, "File path must be entered");

        final SignalContentResponse response = new SignalContentResponse(false);

        File file = new File(filePath);

        Preconditions.checkArgument(file.exists(), "File does not exists");
        Preconditions.checkArgument(!file.isDirectory(), "File path is a directory");
        Preconditions.checkArgument(file.canRead(), "File is unreadable");

        try (InputStream is = new FileInputStream(file)) {
            AudioReader reader = new AudioReader();

            byte[] bytes = reader.read(is);

            Preconditions.checkArgument(bytes.length > 0, "File content is empty");

            int size = bytes.length > LIMIT ? LIMIT : bytes.length;

            List<Integer> signals = IntStream.range(0, size)
                    .map(idx -> (int) bytes[idx])
                    .boxed()
                    .collect(Collectors.toList());

            response.setStatus(true);
            response.setSignals(signals);

            if (size == LIMIT) {
                String message = "Number of signals are probably greater than %d, %d";

                response.setMessage(String.format(message, LIMIT, bytes.length));
            }
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            LOGGER.error(e.getMessage(), e);
        }

        return response;
    }
}