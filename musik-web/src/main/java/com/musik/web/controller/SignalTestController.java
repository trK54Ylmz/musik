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
import com.google.common.io.ByteSource;
import com.musik.io.AudioReader;
import com.musik.utils.ResponseUtils;
import com.musik.web.response.SignalContentResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/test")
public class SignalTestController {
    private static final Logger LOGGER = Logger.getLogger(SignalTestController.class);

    private static final AudioReader READER = new AudioReader();

    private static final int LIMIT = 4096;

    @RequestMapping
    public String getContentPage(Model model) {
        model.addAttribute("title", "Test signal sources");

        return "test";
    }

    @RequestMapping(value = "/native", produces = ResponseUtils.JSON, method = RequestMethod.POST)
    @ResponseBody
    public SignalContentResponse getNativeSignals(@RequestBody String data) {
        SignalContentResponse response = new SignalContentResponse(false);

        try {
            if (!data.startsWith("data:audio/mp3")) {
                response.setMessage("Invalid request body");

                return response;
            }

            byte[] content = Base64.getDecoder().decode(data.substring(22));

            byte[] signals = READER.read(ByteSource.wrap(content).openStream());

            List<Integer> list = new ArrayList<>();
            for (byte signal : signals) {
                list.add((int) signal);
            }

            response.setSignals(list);
            response.setStatus(true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return response;
    }

    @RequestMapping(value = "/content")
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

            List<Integer> signals = IntStream
                    .range(0, size)
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