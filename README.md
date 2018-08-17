# Musik: music recognition application

[![Build Status](https://travis-ci.org/trK54Ylmz/musik.svg?branch=master)](https://travis-ci.org/trK54Ylmz/musik)

Musik is a simple music recognition application

**Please note that this is an ongoing project**

# Prerequisites

* Apache Flink >= 1.2.1
* Apache Cassandra >= 2.0
* Apache Thrift = 0.11.0 (for development)

# Installation

```bash
$ mvn clean package -DskipTests
```

# Usage

There are there parts in the Musik. 

The first one is indexing the audio files (MP3),

```bash
$ flink run musik-index-0.1.jar -input file:///tmp/music -host 127.0.0.1 \
    -port 9042 -db musik -username cassandra -password cassandra -table signals
```

The second one is analyzing audio files on the real time,

```bash
$ flink run musik-index-server-0.1.jar -host 127.0.0.1 -port 9042 \
    -db musik -username cassandra -password cassandra -table signals \
    -name musik -rpc_port 8081
```

and the third one is user interface,
 
```bash
$ java -jar musik-web-0.1.jar -port 8080 -rpc_host=localhost -rpc_port 8081
```

# Test

# Special thanks

* Andrew Embury - octo loader - [https://dribbble.com/aembury]
* Filippo Valsorda - mkcert - [https://github.com/FiloSottile/mkcert]
