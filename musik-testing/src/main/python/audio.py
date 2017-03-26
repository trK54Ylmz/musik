# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import sys

import numpy
import pylab
from numpy.fft import fft
from numpy.fft import fftfreq

from musik.config import ConfigFactory
from musik.io import AudioReader
from musik.utils.log import Logger

logger = Logger.get_logger('audio')

args = {
    '--file': 'Audio file path'
}

config_parser = ConfigFactory()
config_parser.add_argument(args)

config = config_parser.parse(sys.argv[1:])

reader = AudioReader(config.file)

data = reader.get_data()

res = fft(data.get_array_of_samples())

freq = fftfreq(len(res), 10)

pylab.figure()
pylab.plot(freq, numpy.abs(res))
pylab.figure()
pylab.plot(freq, numpy.angle(res))
pylab.show()
