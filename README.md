# Musik: music recognition application

Musik is a simple music recognition application

**Please note that this is an ongoing project**

# Prerequisites

* Apache Flink >= 1.2.1
* Apache Cassandra >= 2.0
* Redis >= 2.9.0
* Apache Kafka = 0.10.0

# Installation

```bash
$ mvn clean package -DskipTests
```

# Usage

There are two parts in the Musik. 

The first one is indexing the audio files (MP3),

```bash
$ flink run musik-index-batch-0.1.jar -input file:///tmp/music -host 127.0.0.1 \
    -port 9042 -db musik -username cassandra -password cassandra -table signals
```

and the second one is analyzing audio files on the real time,

```bash
$ flink run musik-index-streaming-0.1.jar -host 127.0.0.1 -port 9042 \
    -db musik -username cassandra -password cassandra -table signals \
    -kafka localhost:9200 -zookeeper localhost:8020 -topics musik \
    -group_id web -redis localhost:6379 -name musik
```