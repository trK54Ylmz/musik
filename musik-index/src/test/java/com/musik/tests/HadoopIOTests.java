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

import com.google.common.io.Files;

import com.musik.fs.BinaryFileInputFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HadoopIOTests {
    private static Configuration conf;

    private static File file;

    private static Path path;

    @BeforeClass
    public static void setUp() {
        conf = new Configuration(false);
        conf.set("fs.default.name", "file:///");

        file = new File(HadoopIOTests.class.getResource("/sample.mp3").getFile());
        path = new Path(file.getAbsoluteFile().toURI());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testName() throws IOException, InterruptedException {
        FileSplit split = new FileSplit(path, 0, file.length(), null);

        InputFormat inputFormat = ReflectionUtils.newInstance(BinaryFileInputFormat.class, conf);
        TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());

        RecordReader<Text, BytesWritable> reader = inputFormat.createRecordReader(split, context);

        // initialize reader
        reader.initialize(split, context);

        // get file name from current key
        String fileName = Files.getNameWithoutExtension(reader.getCurrentKey().toString());

        // read input split
        reader.nextKeyValue();

        byte[] observed = Arrays.copyOfRange(reader.getCurrentValue().getBytes(), 0, 6);
        byte[] expected = {73, 68, 51, 3, 0, 0};

        assertThat(fileName, is("sample"));
        assertThat(Arrays.equals(observed, expected), is(true));
    }
}