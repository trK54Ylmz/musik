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

import com.google.common.io.ByteStreams;

import com.musik.Utils;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class);

    @RequestMapping("/")
    public String getMainPage(Model model) {
        model.addAttribute("title", "Main page");
        model.addAttribute("sample", Utils.SAMPLE_SIZE);

        return "index";
    }

    @RequestMapping(value = "/favicon.ico", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getFavicon() {
        try (InputStream is = getClass().getResourceAsStream("/assets/images/favicon.png")) {
            return ByteStreams.toByteArray(is);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new byte[]{0};
    }
}