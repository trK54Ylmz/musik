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

package com.musik.tests;

import com.google.common.base.Preconditions;

import com.musik.io.AudioReader;

import org.junit.BeforeClass;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public abstract class TestUtil {
    protected static byte[] bytes;

    @BeforeClass
    public static void setUp() throws IOException {
        URL resource = TestUtil.class.getResource("/sample.mp3");

        Preconditions.checkNotNull(resource, "Sample mp3 file does not exists");

        AudioReader reader = new AudioReader();
        bytes = reader.read(resource.getFile());
    }

    static void draw(int[] bytes, String name) throws IOException {
        int width = bytes.length;
        int height = 0;

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] > height) {
                height = bytes[i];
            }
        }

        height = height * 2 + 1;

        if (width * height > 1048576) {
            while (width * height > 1048576) {
                width /= 2;
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setBackground(Color.white);
        graphics.setPaint(Color.blue);

        int half = height / 2;
        int zip = bytes.length / width;

        for (int i = 0; i < width; i++) {
            graphics.drawLine(i, half, i, half - bytes[i * zip]);
            graphics.drawLine(i, half, i, half);
            graphics.drawLine(i, half, i, half + bytes[i * zip]);
        }

        ImageIO.write(image, "png", new File("/tmp/" + name + ".png"));
    }
}