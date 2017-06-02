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

package com.musik.io;

import com.google.common.base.Preconditions;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioReader {
    private static final AudioFormat FORMAT = new AudioFormat(44100, 8, 2, true, true);

    private static final MpegAudioFileReader READER = new MpegAudioFileReader();

    /**
     * Read audio file from local directory and converts the content of song file as byte array
     *
     * @param path the local file path of audio file
     * @return the content of audio file
     */
    public byte[] read(String path) throws IOException {
        Preconditions.checkNotNull(path, "File path name is empty");

        try (AudioInputStream is = READER.getAudioInputStream(new File(path));
             AudioInputStream ais = AudioSystem.getAudioInputStream(FORMAT, is);
             ByteArrayOutputStream bos = new ByteArrayOutputStream(ais.available())) {
            int read;
            final byte[] buffer = new byte[4096];

            while ((read = ais.read(buffer)) > -1) {
                bos.write(buffer, 0, read);
            }

            return bos.toByteArray();
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Read audio file from input stream and converts the content of song file as byte array
     *
     * @param audio the stream object of audio file
     * @return the content of audio file
     */
    public byte[] read(InputStream audio) throws IOException {
        Preconditions.checkNotNull(audio, "Audio signal is empty");

        try (AudioInputStream is = READER.getAudioInputStream(audio);
             AudioInputStream ais = AudioSystem.getAudioInputStream(FORMAT, is);
             ByteArrayOutputStream bos = new ByteArrayOutputStream(ais.available())) {
            int read;
            final byte[] buffer = new byte[4096];

            while ((read = ais.read(buffer)) > -1) {
                bos.write(buffer, 0, read);
            }

            return bos.toByteArray();
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}