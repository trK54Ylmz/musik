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

import argparse


class ConfigFactory:
    def __init__(self):
        self.parser = argparse.ArgumentParser()

    def add_argument(self, argument, description=None):
        """


        :param argument:
        :param description:
        :return:
        """
        if type(argument) == dict:
            for arg in argument:
                self.parser.add_argument(arg, help=argument[arg])
        elif type(argument) == list:
            for arg in argument:
                self.parser.add_argument(arg)
        else:
            self.parser.add_argument(argument, help=description)

    def parse(self, args):
        """
        Parse command line arguments according to expected arguments

        :param args: the command line arguments
        :return:
        """
        return self.parser.parse_args(args)
